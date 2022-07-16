package com.jxai.module.camera.ui

import com.alibaba.android.arouter.facade.annotation.Route
import com.jxai.lib.common.constant.RouterURL
import com.jxai.lib.common.ui.CommonActivity
import com.jxai.module.camera.R
import com.jxai.module.camera.mvp.CameraPresenter
import com.jxai.module.camera.mvp.ICameraView
import kotlinx.android.synthetic.main.activity_camera.*

@Route(path = RouterURL.CAMERA_ACTIVITY, name = "CameraActivity")
class CameraActivity : CommonActivity<ICameraView,CameraPresenter>(),ICameraView {

    override fun getLayout(): Int {
        return R.layout.activity_camera
    }

    override fun initPresenter(): CameraPresenter {
        return CameraPresenter()
    }

    override fun initView() {

    }

    override fun initEvent() {
        picture.setOnClickListener {
            p?.requestServerVerify()
        }
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun netError() {

    }
}