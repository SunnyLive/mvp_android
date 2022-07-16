package com.jxai.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter
import com.jxai.lib.common.constant.RouterURL
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_to_camera.setOnClickListener {
            ARouter.getInstance().build(RouterURL.CAMERA_ACTIVITY).navigation()
        }
    }
}