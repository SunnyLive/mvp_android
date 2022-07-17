package com.jxai.camera

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter
import com.jxai.lib.common.constant.RouterURL
import com.jxai.lib.core.ui.BaseRxActivity
import com.jxai.lib.utils.ext.toastShortShow
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseRxActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        RxPermissions(this).request(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).compose(bindToLifecycle())
            .subscribe {
                if (!it) {
                    toastShortShow( "您关闭了权限，请去设置页面开启")
                }
            }

        btn_to_camera.setOnClickListener {
            ARouter.getInstance().build(RouterURL.CAMERA_ACTIVITY).navigation()
        }
    }


    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun netError() {

    }
}