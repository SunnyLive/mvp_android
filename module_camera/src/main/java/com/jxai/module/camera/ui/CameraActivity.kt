package com.jxai.module.camera.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.jxai.lib.common.constant.RouterURL
import com.jxai.lib.common.ui.CommonActivity
import com.jxai.lib.utils.ext.toastShortShow
import com.jxai.module.camera.R
import com.jxai.module.camera.mvp.CameraPresenter
import com.jxai.module.camera.mvp.ICameraView
import com.orhanobut.logger.Logger
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_camera.*
import java.util.concurrent.TimeUnit

@Route(path = RouterURL.CAMERA_ACTIVITY, name = "CameraActivity")
class CameraActivity : CommonActivity<ICameraView,CameraPresenter>(),ICameraView {

    override fun getLayout(): Int {
        return R.layout.activity_camera
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.container, CameraFragment.newInstance())
            .commit()
    }

    override fun initPresenter(): CameraPresenter {
        return CameraPresenter()
    }

    override fun initView() {

    }

    override fun initEvent() {
        //p?.requestServerVerify()
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun netError() {

    }



    @SuppressLint("CheckResult")
    private fun requestCameraPermission() {
        RxPermissions(this).request(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).compose(bindToLifecycle())
            .subscribe {
                if (!it) {
                    toastShortShow( "您关闭了权限，请去设置页面开启")
                    finish()
                }
            }
    }
}