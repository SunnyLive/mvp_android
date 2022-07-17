package com.jxai.module.camera.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import androidx.core.content.ContextCompat
import com.example.android.camera2basic.ErrorDialog
import com.jxai.lib.common.view.AutoFitTextureView
import com.jxai.lib.core.ui.BaseRxFragment
import com.jxai.lib.utils.ext.toastShortShow
import com.jxai.module.camera.R
import com.jxai.module.camera.core.*
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


/**
 *
 * 这是一个camera程序
 * 是用camera2的api开发的
 *
 */
class CameraFragment : BaseRxFragment(), View.OnClickListener {

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun netError() {

    }


    /**
     * [TextureView.SurfaceTextureListener] 处理多个生命周期事件
     * [TextureView].
     */
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit

    }

    /**
     * ID  [CameraDevice]. 当前的CameraId
     */
    private lateinit var cameraId: String

    /**
     * An [AutoFitTextureView] 对相机预览.
     */
    private lateinit var textureView: AutoFitTextureView

    /**
     * A [CameraCaptureSession] 对相机预览.
     */
    private var captureSession: CameraCaptureSession? = null

    /**
     * A 参考已打开的 [CameraDevice].
     */
    private var cameraDevice: CameraDevice? = null

    /**
     * [android.util.Size] 相机预览的大小。
     */
    private lateinit var previewSize: Size

    /**
     * [CameraDevice。当[CameraDevice]改变它的状态时，StateCallback被调用。
     */
    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@CameraFragment.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraFragment.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
            this@CameraFragment.activity?.finish()
        }

    }

    /**
     * 一个额外的线程，用于运行不会阻塞UI的任务。
     */
    private var backgroundThread: HandlerThread? = null

    /**
     * A [Handler] 用于在后台运行任务。
     */
    private var backgroundHandler: Handler? = null

    /**
     * An [ImageReader] 它处理静态图像捕获。
     */
    private var imageReader: ImageReader? = null

    /**
     * 这是我们图片的输出文件。
     */
    private lateinit var file: File

    /**
     * 这是[ImageReader]的回调对象。“onImageAvailable”将被调用
     * 静止图像可以保存了。
     */
    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        backgroundHandler?.post(ImageSaver(it.acquireNextImage(), file))
    }

    /**
     * [CaptureRequest.Builder] 对于相机预览
     */
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    /**
     * [CaptureRequest] generated by [previewRequestBuilder]
     */
    private lateinit var previewRequest: CaptureRequest

    /**
     * 拍照时相机的当前状态。
     *
     * @see captureCallback
     */
    private var state = STATE_PREVIEW

    /**
     * A [Semaphore] 为了防止应用程序退出前关闭相机。
     */
    private val cameraOpenCloseLock = Semaphore(1)

    /**
     * 当前摄像设备是否支持Flash。
     */
    private var flashSupported = false

    /**
     * 相机传感器的定位
     */
    private var sensorOrientation = 0

    /**
     * A [CameraCaptureSession.CaptureCallback] 处理与JPEG捕获相关的事件。
     */
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit // 相机预览正常时，什么都不做。
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE 在某些设备上可以为空
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED
                    ) {
                        state = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE 在某些设备上可以为空
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
            ) {
                // CONTROL_AE_STATE在某些设备上可以为空
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state = STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_fragment_camera, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.picture).setOnClickListener(this)
        textureView = view.findViewById(R.id.texture)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        file = File(activity?.getExternalFilesDir(null), PIC_FILE_NAME)
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        // 当屏幕被关闭并重新打开时，SurfaceTexture已经被打开了
        // 可用，并且"onSurfaceTextureAvailable"不会被调用。那样的话，我们可以开了
        // 一个相机，从这里开始预览(否则，我们等待表面准备好
        // SurfaceTextureListener)。
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * 设置与camera相关的成员变量。
     * @param width 相机预览可用尺寸的宽度
     * @param height 相机预览可用的高度大小
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // 我们在这个样本中没有使用前置摄像头。
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null &&
                    cameraDirection == CameraCharacteristics.LENS_FACING_FRONT
                ) {
                    continue
                }

                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                ) ?: continue

                // 对于静态图像捕获，我们使用最大的可用尺寸。
                val largest = Collections.max(
                    listOf(*map.getOutputSizes(ImageFormat.JPEG)),
                    CompareSizesByArea()
                )
                imageReader = ImageReader.newInstance(
                    largest.width, largest.height,
                    ImageFormat.JPEG, /*maxImages*/ 2
                ).apply {
                    setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
                }

                // 找出我们是否需要交换尺寸来获得相对于传感器的预览大小
                // 坐标。
                val displayRotation = activity?.windowManager?.defaultDisplay?.rotation!!

                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                val swappedDimensions = areDimensionsSwapped(displayRotation)

                val displaySize = Point()
                activity?.windowManager?.defaultDisplay?.getSize(displaySize)
                val rotatedPreviewWidth = if (swappedDimensions) height else width
                val rotatedPreviewHeight = if (swappedDimensions) width else height
                var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
                var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT

                // 危险,得到!尝试使用太大的预览尺寸可能会超出相机的范围
                // bus的带宽限制，导致华丽的预览，但存储 垃圾捕获数据。
                previewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth, rotatedPreviewHeight,
                    maxPreviewWidth, maxPreviewHeight,
                    largest
                )

                // 我们将TextureView的纵横比调整为我们选择的预览大小。
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.width, previewSize.height)
                } else {
                    textureView.setAspectRatio(previewSize.height, previewSize.width)
                }

                // 检查是否支持flash。
                flashSupported =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                this.cameraId = cameraId

                // 我们找到了一台可行的摄像机，并完成了成员变量的设置，
                // 这样我们就不需要遍历其他可用的相机。
                return
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: NullPointerException) {
            //当前，当使用Camera2API但不被支持时，会抛出一个NPE
            //运行此代码的设备。
            ErrorDialog.newInstance(getString(R.string.camera_error))
                .show(childFragmentManager, FRAGMENT_DIALOG)
        }

    }

    /**
     * 确定在给定手机当前旋转的情况下是否交换尺寸。
     * @param displayRotation 显示当前的旋转
     * 如果维度交换，则返回true，否则返回false。
     */
    private fun areDimensionsSwapped(displayRotation: Int): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                Log.e(TAG, "显示旋转无效: $displayRotation")
            }
        }
        return swappedDimensions
    }

    /**
     * 打开指定的相机  [CameraFragment.cameraId].
     */
    private fun openCamera(width: Int, height: Int) {
        if (activity == null) return
        val permission =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
            return
        }
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // 等待相机打开——2.5秒就足够了
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("等待锁定相机打开时间超时.")
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("在试图锁定相机打开时被打断.", e)
        }

    }

    /**
     * 关闭当前 [CameraDevice].
     */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("在试图锁定相机关闭时被打断。", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /**
     * 启动一个后台线程 [Handler].
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper!!)
    }

    /**
     * 停止后台线程及其[Handler]。
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * 创建一个新的[CameraCaptureSession]相机预览。
     */
    private fun createCameraPreviewSession() {
        try {
            val texture = textureView.surfaceTexture

            // 我们配置默认缓冲区的大小为我们想要的相机预览大小。
            texture?.setDefaultBufferSize(previewSize.width, previewSize.height)

            // 这是我们需要开始预览的输出Surface。
            val surface = Surface(texture)

            // 我们设置了一个CaptureRequest。生成器与输出Surface。
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder.addTarget(surface)

            // 在这里，我们创建一个CameraCaptureSession相机预览。
            cameraDevice?.createCaptureSession(
                listOf(surface, imageReader?.surface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // 相机已经关闭了
                        if (cameraDevice == null) return

                        // 当会话准备好时，我们开始显示预览。
                        captureSession = cameraCaptureSession
                        try {
                            // 自动对焦应该是连续的相机预览。
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            // Flash在必要时自动启用。
                            setAutoFlash(previewRequestBuilder)

                            //  最后，我们开始显示相机预览。
                            previewRequest = previewRequestBuilder.build()
                            captureSession?.setRepeatingRequest(
                                previewRequest,
                                captureCallback, backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, e.toString())
                        }

                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        activity?.toastShortShow("Failed")
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     *配置必要的[android.graphics]转换为' textureView '。
     * 这个方法应该在相机预览大小确定后调用
     * [setUpCameraOutputs] 和' textureView '的大小是固定的。
     * @param viewWidth ' textureView '的宽度
     * @param viewHeight ' textureView '的高度
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        activity ?: return
        val rotation = activity?.windowManager?.defaultDisplay?.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale = Math.max(
                viewHeight.toFloat() / previewSize.height,
                viewWidth.toFloat() / previewSize.width
            )
            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    /**
     * 锁定焦点作为捕获静止图像的第一步。
     */
    private fun lockFocus() {
        try {
            // 这就是如何告诉相机锁定焦点。
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            //  告诉#captureCallback等待锁。
            state = STATE_WAITING_LOCK
            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * 运行预捕获序列来捕获静态图像。应该在以下情况调用此方法
     * 我们在[中得到一个响应。captureCallback] [.lockFocus]。
     */
    private fun runPrecaptureSequence() {
        try {
            // 这是如何告诉相机触发。
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            // 告诉#captureCallback等待设置预捕获序列。
            state = STATE_WAITING_PRECAPTURE
            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     *  捕捉静止的画面。当我们在中获得响应时，应该调用此方法
     *  (。captureCallback]从[. lockfocus]。
     */
    private fun captureStillPicture() {
        try {
            if (activity == null || cameraDevice == null) return
            val rotation = activity?.windowManager?.defaultDisplay?.rotation

            //这是CaptureRequest。我们用来拍照的建筑工人。
            val captureBuilder = cameraDevice?.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE
            )?.apply {
                addTarget(imageReader?.surface!!)

                //对于大多数设备，传感器的方向是90，或者对于一些设备(例如。联系5 x)
                //我们必须考虑到这一点，并适当旋转JPEG。
                //对于方向为90的设备，我们从orientation返回映射。
                //对于270方向的设备，我们需要将JPEG旋转180度。
                set(
                    CaptureRequest.JPEG_ORIENTATION,
                    (ORIENTATIONS.get(rotation!!) + sensorOrientation + 270) % 360
                )

                //使用相同的AE和AF模式作为预览。
                set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }?.also { setAutoFlash(it) }

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    activity?.toastShortShow("Saved: $file")
                    Log.d(TAG, file.toString())
                    unlockFocus()
                }
            }

            captureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder?.build()!!, captureCallback, null)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * 解锁的焦点。当静止图像捕获序列为时，应调用此方法完成。
     */
    private fun unlockFocus() {
        try {
            // 重置自动对焦触发器
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
            )
            setAutoFlash(previewRequestBuilder)
            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                backgroundHandler
            )
            // 在此之后，相机将回到正常的预览状态。
            state = STATE_PREVIEW
            captureSession?.setRepeatingRequest(
                previewRequest, captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.picture -> lockFocus()
        }
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }

    companion object {

        /**
         * 从屏幕旋转到JPEG方向的转换。
         */
        private val ORIENTATIONS = SparseIntArray()
        private const val FRAGMENT_DIALOG = "dialog"

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        /**
         * Tag for the [Log].
         */
        private const val TAG = "Camera2BasicFragment"

        /**
         *摄像头状态:显示摄像头预览。
         */
        private const val STATE_PREVIEW = 0

        /**
         * 相机状态:等待对焦。
         */
        private const val STATE_WAITING_LOCK = 1

        /**
         * 相机状态:等待曝光进入预捕捉状态。
         */
        private const val STATE_WAITING_PRECAPTURE = 2

        /**
         * 相机状态:等待曝光状态，而不是预捕捉。
         */
        private const val STATE_WAITING_NON_PRECAPTURE = 3

        /**
         * 相机状态:已拍照。
         */
        private const val STATE_PICTURE_TAKEN = 4

        /**
         * 最大的预览宽度，由Camera2 API保证
         */
        private const val MAX_PREVIEW_WIDTH = 1920

        /**
         * 最大的预览高度，由Camera2 API保证
         */
        private const val MAX_PREVIEW_HEIGHT = 1080

        /**
         * 给出“相机支持的尺寸”的选项，选择最小的一个
         *至少与各自的纹理视图大小一样大，而那是最多与
         *各自的最大尺寸，其长宽比与指定值匹配。如果这样的
         * size不存在，选择最大的一个，最多与各自的Max一样大
         *大小，且长宽比与指定值匹配。
         *
         * @param choices          相机支持的预期尺寸列表
         *                         输出类
         * @param textureViewWidth  纹理视图相对于传感器坐标的宽度
         * @param textureViewHeight 纹理视图相对于传感器坐标的高度
         * @param maxWidth          可选择的最大宽度
         * @param maxHeight         可选择的最大高度
         * @param aspectRatio       高宽比
         * @return 最佳“大小”，或者任意一个，如果没有一个足够大
         */
        @JvmStatic
        private fun chooseOptimalSize(
            choices: Array<Size>,
            textureViewWidth: Int,
            textureViewHeight: Int,
            maxWidth: Int,
            maxHeight: Int,
            aspectRatio: Size
        ): Size {

            // 收集支持的分辨率，至少与预览Surface一样大
            val bigEnough = ArrayList<Size>()
            // 收集比预览Surface小的支持分辨率
            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w
                ) {
                    if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                        bigEnough.add(option)
                    } else {
                        notBigEnough.add(option)
                    }
                }
            }

            // 从那些足够大的中选择最小的。如果没有足够大的，就从那些不够大的中挑最大的。
            return if (bigEnough.size > 0) {
                Collections.min(bigEnough, CompareSizesByArea())
            } else if (notBigEnough.size > 0) {
                Collections.max(notBigEnough, CompareSizesByArea())
            } else {
                Log.e(TAG, "找不到合适的预览大小")
                choices[0]
            }
        }

        @JvmStatic
        fun newInstance(): CameraFragment = CameraFragment()
    }


}