package com.jxai.module.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.jxai.lib.common.constant.RouterURL

@Route(path = RouterURL.CAMERA_ACTIVITY, name = "CameraActivity")
class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
    }
}