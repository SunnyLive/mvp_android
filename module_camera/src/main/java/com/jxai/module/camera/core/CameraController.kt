package com.jxai.module.camera.core

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.jxai.lib.common.view.AutoFitTextureView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Author: baipenggui
 * Date: 2022/3/17 10:49
 * Email: 1528354213@qq.com
 * Description:
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class CameraController private constructor() {
    private var mFolderPath //保存视频,图片的文件夹路径
            : String? = null
    private var mImageReader: ImageReader? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private var mTextureView: AutoFitTextureView? = null
    private val mCameraOpenCloseLock = Semaphore(1)                 //一个信号量以防止应用程序在关闭相机之前退出。
    private var mCameraId: String? = null                                   //当前相机的ID。

    private var mCameraDevice: CameraDevice? = null
    private var mPreviewSize: Size? = null
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mPreviewRequest: CaptureRequest? = null
    private var mFile: File? = null                                         //拍照储存文件

    private var mSensorOrientation: Int? = null
    private var mPreviewSession: CameraCaptureSession? = null
    private var mPreviewBuilder: CaptureRequest.Builder? = null
    private val mFlashSupported = false
    private var mState = STATE_PREVIEW

    companion object {
        private const val TAG = "CameraController"
        private var mActivity: Activity? = null
        private const val MAX_PREVIEW_WIDTH = 1920                          //Camera2 API保证的最大预览宽度
        private const val MAX_PREVIEW_HEIGHT = 1080                         //Camera2 API保证的最大预览高度
        private val ORIENTATIONS = SparseIntArray()
        private const val SENSOR_ORIENTATION_DEFAULT_DEGREES = 90
        private const val SENSOR_ORIENTATION_INVERSE_DEGREES = 270
        private const val STATE_PREVIEW = 0                                 //相机状态：显示相机预览。
        private const val STATE_WAITING_LOCK = 1                            //相机状态：等待焦点被锁定。
        private const val STATE_WAITING_PRECAPTURE = 2                      //等待曝光被Precapture状态。
        private const val STATE_WAITING_NON_PRECAPTURE = 3                  //相机状态：等待曝光的状态是不是Precapture。
        private const val STATE_PICTURE_TAKEN = 4                           //相机状态：拍照。

        fun getInstance(activity: Activity?): CameraController {
            mActivity = activity
            return ClassHolder.mInstance
        }

        private fun chooseOptimalSize(
            choices: Array<Size>, textureViewWidth: Int,
            textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size
        ): Size {

            // 收集支持的分辨率，这些分辨率至少与预览图面一样大
            val bigEnough: MutableList<Size> = ArrayList()
            // 收集小于预览表面的支持分辨率
            val notBigEnough: MutableList<Size> = ArrayList()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                    if (option.width >= textureViewWidth &&
                        option.height >= textureViewHeight
                    ) {
                        bigEnough.add(option)
                    } else {
                        notBigEnough.add(option)
                    }
                }
            }

            //挑一个足够大的最小的。如果没有一个足够大的，就挑一个不够大的。
            return if (bigEnough.size > 0) {
                Collections.min(bigEnough, CompareSizesByArea())
            } else if (notBigEnough.size > 0) {
                Collections.max(notBigEnough, CompareSizesByArea())
            } else {
                Log.d(TAG, "Couldn't find any suitable preview size")
                choices[0]
            }
        }

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 0)
            ORIENTATIONS.append(Surface.ROTATION_90, 900)
            ORIENTATIONS.append(Surface.ROTATION_180, 180)
            ORIENTATIONS.append(Surface.ROTATION_270, 270)
        }
    }

    private var mMediaRecorder: MediaRecorder? = null
    private var mNextVideoAbsolutePath: String? = null

    private object ClassHolder {
        var mInstance = CameraController()
    }

    fun initCamera(textureView: AutoFitTextureView?) {
        mTextureView = textureView
        startBackgroundThread()
        if (mTextureView!!.isAvailable) {
            openCamera(mTextureView!!.width, mTextureView!!.height)
        } else {
            mTextureView!!.surfaceTextureListener = mSurfaceTextureListener
        }
    }

    /**
     * 设置需要保存文件的文件夹路径
     * @param path
     */
    var folderPath: String?
        get() = mFolderPath
        set(path) {
            mFolderPath = path
            val mFolder = File(path)
            if (!mFolder.exists()) {
                mFolder.mkdirs()
                Log.d(TAG, "文件夹不存在去创建")
            } else {
                Log.d(TAG, "文件夹已创建")
            }
        }

    /**
     * 拍照
     */
    fun takePicture() {
        lockFocus()
    }

    /**
     * 开始录像
     */
    fun startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView!!.isAvailable || null == mPreviewSize) {
            return
        }
        try {
            closePreviewSession()
            setUpMediaRecorder()
            val texture = mTextureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            val surfaces: MutableList<Surface> = ArrayList()

            //为相机预览设置曲面
            val previewSurface = Surface(texture)
            surfaces.add(previewSurface)
            mPreviewBuilder!!.addTarget(previewSurface)

            //设置MediaRecorder的表面
            val recorderSurface = mMediaRecorder!!.surface
            surfaces.add(recorderSurface)
            mPreviewBuilder!!.addTarget(recorderSurface)

            // 启动捕获会话
            // 一旦会话开始，我们就可以更新UI并开始录制
            mCameraDevice!!.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        mPreviewSession = cameraCaptureSession
                        updatePreview()
                        mActivity!!.runOnUiThread { //开启录像
                            mMediaRecorder!!.start()
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {}
                },
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 停止录像
     */
    fun stopRecordingVideo() {
        mMediaRecorder!!.stop()
        mMediaRecorder!!.reset()
        mNextVideoAbsolutePath = null
        startPreview()
    }

    private fun closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession!!.close()
            mPreviewSession = null
        }
    }

    @Throws(IOException::class)
    private fun setUpMediaRecorder() {
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)              //设置用于录制的音源
        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)          //开始捕捉和编码数据到setOutputFile（指定的文件）
        mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)         //设置在录制过程中产生的输出文件的格式
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath!!.isEmpty()) {
            mNextVideoAbsolutePath = videoFilePath
        }
        mMediaRecorder!!.setOutputFile(mNextVideoAbsolutePath)                      //设置输出文件的路径
        mMediaRecorder!!.setVideoEncodingBitRate(10000000)                          //设置录制的视频编码比特率
        mMediaRecorder!!.setVideoFrameRate(25)                                      //设置要捕获的视频帧速率
        mMediaRecorder!!.setVideoSize(mPreviewSize!!.width, mPreviewSize!!.height)  //设置要捕获的视频的宽度和高度
        mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)           //设置视频编码器，用于录制
        mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)            //设置audio的编码格式
        val rotation = mActivity!!.windowManager.defaultDisplay.rotation
        Log.d(TAG, "setUpMediaRecorder: $rotation")
        when (mSensorOrientation) {
            SENSOR_ORIENTATION_DEFAULT_DEGREES -> mMediaRecorder!!.setOrientationHint(
                ORIENTATIONS[rotation]
            )
            SENSOR_ORIENTATION_INVERSE_DEGREES -> mMediaRecorder!!.setOrientationHint(
                ORIENTATIONS[rotation]
            )
        }
        mMediaRecorder!!.prepare()
    }

    private val videoFilePath: String
        private get() = folderPath + "/" + System.currentTimeMillis() + ".mp4"

    private fun updatePreview() {
        if (null == mCameraDevice) {
            return
        }
        try {
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            val thread = HandlerThread("CameraPreview")
            thread.start()
            mPreviewSession!!.setRepeatingRequest(
                mPreviewBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun lockFocus() {

        //创建文件
        mFile = File("$folderPath/$nowDate.jpg")
        try {
            mPreviewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            mState = STATE_WAITING_LOCK
            mCaptureSession?.capture(
                mPreviewRequestBuilder!!.build(), mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * 获取当前时间,用来给文件夹命名
     */
    private val nowDate: String
        private get() {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return simpleDateFormat.format(Date())
        }
    private val mSurfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    /**
     * 开启相机
     */
    private fun openCamera(width: Int, height: Int) {
        //设置相机输出
        setUpCameraOutputs(width, height)
        //配置变换
        configureTransform(width, height)
        val manager = mActivity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            if (ActivityCompat.checkSelfPermission(
                    mActivity!!,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            //打开相机预览
            manager.openCamera(mCameraId!!, mStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    /**
     * CameraDevice状态更改时被调用。
     */
    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            // 打开相机时调用此方法。 在这里开始相机预览。
            mCameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            //创建CameraPreviewSession
            startPreview()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
            mActivity!!.finish()
        }
    }

    /**
     * 为相机预览创建新的CameraCaptureSession
     */
    private fun startPreview() {
        try {
            val texture = mTextureView!!.surfaceTexture!!

            // 将默认缓冲区的大小配置为我们想要的相机预览的大小。 设置分辨率
            texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)

            // 预览的输出Surface。
            val surface = Surface(texture)

            //设置了一个具有输出Surface的CaptureRequest.Builder。
            mPreviewRequestBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder!!.addTarget(surface)

            //创建一个CameraCaptureSession来进行相机预览。
            mCameraDevice!!.createCaptureSession(
                Arrays.asList(surface, mImageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // 相机已经关闭
                        if (null == mCameraDevice) {
                            return
                        }

                        //会话准备好后，我们开始显示预览
                        mCaptureSession = cameraCaptureSession
                        // 自动对焦应
                        mPreviewRequestBuilder!!.set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                        )

                        // 最终开启相机预览并添加事件
                        mPreviewRequest = mPreviewRequestBuilder!!.build()
                        try {
                            mCaptureSession!!.setRepeatingRequest(
                                mPreviewRequest!!,
                                mCaptureCallback, mBackgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(
                        cameraCaptureSession: CameraCaptureSession
                    ) {
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * 处理与JPEG捕获有关的事件
     */
    private val mCaptureCallback: CaptureCallback = object : CaptureCallback() {
        //处理
        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_PREVIEW -> {}
                STATE_WAITING_LOCK -> {

                    //等待对焦
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                    ) {
                        // 某些设备上的控制状态可以为空
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN
                            captureStillPicture()
                        } else {
                            runPrecaptureSequence()
                        }
                    } else {
                        mState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
                STATE_WAITING_PRECAPTURE -> {

                    //某些设备上的控制状态可以为空
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {

                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
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

    /**
     * 拍摄静态图片。
     */
    private fun captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return
            }
            // 这是用来拍摄照片的CaptureRequest.Builder。
            val captureBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader!!.surface)

            // 使用相同的AE和AF模式作为预览。
            captureBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            setAutoFlash(captureBuilder)

            // 方向
            val rotation = mActivity!!.windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))
            val CaptureCallback: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    showToast("图片地址: $mFile")
                    Log.d(TAG, mFile.toString())
                    unlockFocus()
                }
            }
            //停止连续取景
            mCaptureSession!!.stopRepeating()
            //捕获图片
            mCaptureSession!!.capture(captureBuilder.build(), CaptureCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun showToast(text: String) {
        mActivity!!.runOnUiThread { Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show() }
    }

    private fun getOrientation(rotation: Int): Int {
        return (ORIENTATIONS[rotation] + mSensorOrientation!! + 270) % 360
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder?) {
        if (mFlashSupported) {
            requestBuilder!!.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }

    /**
     * 解锁焦点
     */
    private fun unlockFocus() {
        try {
            // 重置自动对焦
            mPreviewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
            )
            setAutoFlash(mPreviewRequestBuilder)
            mCaptureSession!!.capture(
                mPreviewRequestBuilder!!.build(), mCaptureCallback,
                mBackgroundHandler
            )
            // 将相机恢复正常的预览状态。
            mState = STATE_PREVIEW
            // 打开连续取景模式
            mCaptureSession!!.setRepeatingRequest(
                mPreviewRequest!!, mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * 运行捕获静止图像的预捕获序列。 当我们从[（）][.]的[.mCaptureCallback]中得到响应时，应该调用此方法。
     */
    private fun runPrecaptureSequence() {
        try {
            // 这是如何告诉相机触发的。
            mPreviewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            // 告诉 mCaptureCallback 等待preapture序列被设置.
            mState = STATE_WAITING_PRECAPTURE
            mCaptureSession!!.capture(
                mPreviewRequestBuilder!!.build(), mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    /**
     *
     * 图片拍照完成后
     * 是否需要通过回调方法处理后续
     *
     * 如： 是否需要base64
     */
    private var mEnCode: ((file: File) -> Unit)? = null

    fun setEnCodeListener(encode: (file: File) -> Unit) {
        mEnCode = encode
    }


    private val mOnImageAvailableListener = OnImageAvailableListener { reader -> //等图片可以得到的时候获取图片并保存
        mBackgroundHandler!!.post(object : ImageSaver(reader.acquireNextImage(), mFile!!) {
            override fun onEncode(file: File) {
                mEnCode?.invoke(file)
            }
        })
    }

    /**
     * 设置与相机相关的成员变量。
     *
     * @param width  相机预览的可用尺寸宽度
     * @param height 相机预览的可用尺寸的高度
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = mActivity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        //获取摄像头列表
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                //不使用前置摄像
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue  //不结束循环,只跳出本次循环
                }
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: continue
                //对于静态图像拍照, 使用最大的可用尺寸
                val largest = Collections.max(
                    Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                    CompareSizesByArea()
                )
                mImageReader =
                    ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, 2)
                mImageReader!!.setOnImageAvailableListener(
                    mOnImageAvailableListener,
                    mBackgroundHandler
                )
                //获取手机旋转的角度以调整图片的方向
                val displayRotation = mActivity!!.windowManager.defaultDisplay.rotation
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 ->                         //横屏
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true
                        }
                    Surface.ROTATION_90, Surface.ROTATION_270 ->                         //竖屏
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true
                        }
                    else -> Log.e(TAG, "Display rotation is invalid: $displayRotation")
                }
                val displaySize = Point()
                mActivity!!.windowManager.defaultDisplay.getSize(displaySize)
                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxPreviewWidth = displaySize.x
                var maxPreviewHeight = displaySize.y
                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                }
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH
                }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
                }
                val displayMetrics = DisplayMetrics()
                mActivity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
                val widthPixels = displayMetrics.widthPixels
                val heightPixels = displayMetrics.heightPixels
                Log.d(TAG, "widthPixels: " + widthPixels + "____heightPixels:" + heightPixels)

                //尝试使用太大的预览大小可能会超出摄像头的带宽限制
                mPreviewSize = chooseOptimalSize(
                    map.getOutputSizes(
                        SurfaceTexture::class.java
                    ),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest
                )

                //我们将TextureView的宽高比与我们选择的预览大小相匹配。这样设置不会拉伸,但是不能全屏展示
                val orientation = mActivity!!.resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    //横屏
                    mTextureView!!.setAspectRatio(mPreviewSize!!.width, mPreviewSize!!.height)
                    Log.d(
                        TAG,
                        "横屏: " + "width:" + mPreviewSize!!.width + "____height:" + mPreviewSize!!.height
                    )
                } else {
                    // 竖屏
                    mTextureView!!.setAspectRatio(widthPixels, heightPixels)
                    Log.d(
                        TAG,
                        "竖屏: " + "____height:" + mPreviewSize!!.height + "width:" + mPreviewSize!!.width
                    )
                }
                mMediaRecorder = MediaRecorder()
                mCameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
        }
    }

    /**
     * 根据他们的区域比较两个Size
     */
    internal class CompareSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            // 我们在这里投放，以确保乘法不会溢出
            return java.lang.Long.signum(
                lhs.width.toLong() * lhs.height -
                        rhs.width.toLong() * rhs.height
            )
        }
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        if (null == mTextureView || null == mPreviewSize) {
            return
        }
        val rotation = mActivity!!.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(
            0f, 0f, mPreviewSize!!.height.toFloat(), mPreviewSize!!.width
                .toFloat()
        )
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                viewHeight.toFloat() / mPreviewSize!!.height,
                viewWidth.toFloat() / mPreviewSize!!.width
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        mTextureView!!.setTransform(matrix)
    } //    /**
    //     * 将JPG保存到指定的文件中。
    //     */
    //    private static class ImageSaver implements Runnable {
    //
    //        /**
    //         * JPEG图像
    //         */
    //        private final Image mImage;
    //        /**
    //         * 保存图像的文件
    //         */
    //        private final File mFile;
    //
    //        public ImageSaver(Image image, File file) {
    //            mImage = image;
    //            mFile = file;
    //        }
    //
    //        @Override
    //        public void run() {
    //            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
    //            byte[] bytes = new byte[buffer.remaining()];
    //            buffer.get(bytes);
    //            FileOutputStream output = null;
    //            try {
    //                output = new FileOutputStream(mFile);
    //                output.write(bytes);
    //            } catch (IOException e) {
    //                e.printStackTrace();
    //            } finally {
    //                mImage.close();
    //                if (null != output) {
    //                    try {
    //                        output.close();
    //                    } catch (IOException e) {
    //                        e.printStackTrace();
    //                    }
    //                }
    //            }
    //        }
    //
    //    }
}