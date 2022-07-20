package com.jxai.module.camera.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jxai.lib.core.ui.BaseRxFragment
import com.jxai.module.camera.R
import com.jxai.module.camera.core.CameraController
import kotlinx.android.synthetic.main.layout_fragment_camera.*
import java.io.File


/**
 *
 * 这是一个camera程序
 * 是用camera2的api开发的
 *
 */
class CameraFragment : BaseRxFragment() {

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun netError() {

    }

    /**
     *
     * 开始停止录像
     *
     */
    private var mIsRecordingVideo = false

    /**
     *
     * 文件存储路径
     *
     */
    var mPicturePath: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .toString() + "/jx_camera"
    fun setPicturePath(mPicturePath: String) {
        this.mPicturePath = mPicturePath
    }


    /**
     *
     * 图片拍照完成后
     * 是否需要通过回调方法处理后续
     *
     * 如： 是否需要base64
     */
    private var mEnCode : ((file: File)->Unit)?=null

    fun setPictureEnCodeListener(encode:(file: File)->Unit){
        mEnCode = encode
    }

    private var mCameraController: CameraController? = null

    /**
     * 提供外部调用 拍照快门
     */
    fun takePicture(){
        doSide()
    }

    /**
     *
     * 立即录像
     *
     */
    fun takeVideo(){
        doVideo()
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_fragment_camera, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //获取相机管理类的实例
        mCameraController = CameraController.getInstance(activity)
        mCameraController?.apply {
            folderPath = mPicturePath
            setEnCodeListener{
                mEnCode?.invoke(it)
            }
        }

    }


    override fun onResume() {
        super.onResume()

        //判断当前屏幕方向
        if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            //竖屏时
            horizontal_linear.visibility = View.VISIBLE
            vertical_linear.visibility = View.GONE
        } else {
            //横屏时
            vertical_linear.visibility = View.VISIBLE
            horizontal_linear.visibility = View.GONE
        }
        mCameraController?.initCamera(textureview)

        //开始拍照
        take_picture_btn.setOnClickListener { doSide() }
        take_picture_btn2.setOnClickListener { doSide() }

        //开始录像
        video_recode_btn.setOnClickListener { doVideo() }
        video_recode_btn2.setOnClickListener { doVideo() }

        //横竖屏切换
        v_h_screen_btn.setOnClickListener {
            //判断当前屏幕方向
            //判断当前屏幕方向
            activity?.apply {
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    //切换竖屏
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                } else {
                    //切换横屏
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        }
    }

    /**
     *
     * 开始拍照
     *
     */
    private fun doSide() {
        mCameraController?.takePicture()
    }

    /**
     *
     * 开始录像
     *
     */
    private fun doVideo() {
        if (mIsRecordingVideo) {
            mIsRecordingVideo = !mIsRecordingVideo
            mCameraController!!.stopRecordingVideo()
            video_recode_btn.text = "开始录像"
            video_recode_btn2.text = "开始录像"
            Toast.makeText(activity, "录像结束", Toast.LENGTH_SHORT).show()
        } else {
            video_recode_btn.text = "停止录像"
            video_recode_btn2.text = "停止录像"
            mIsRecordingVideo = !mIsRecordingVideo
            mCameraController!!.startRecordingVideo()
            Toast.makeText(activity, "录像开始", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onPause() {
        super.onPause()
    }


    companion object {
        fun newInstance(): CameraFragment = CameraFragment()
    }


}