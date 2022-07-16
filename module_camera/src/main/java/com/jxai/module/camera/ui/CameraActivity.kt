package com.jxai.module.camera.ui

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.jxai.lib.common.constant.RouterURL
import com.jxai.lib.common.ui.CommonActivity
import com.jxai.module.camera.R
import com.jxai.module.camera.mvp.CameraPresenter
import com.jxai.module.camera.mvp.ICameraView

@Route(path = RouterURL.CAMERA_ACTIVITY, name = "CameraActivity")
class CameraActivity : CommonActivity<ICameraView,CameraPresenter>(),ICameraView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
    }

    override fun initPresenter(): CameraPresenter {
        return CameraPresenter()
    }

    override fun initView() {

    }

    override fun initEvent() {

    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun netError() {

    }
}