package com.jxai.camera.config

import com.jxai.camera.BuildConfig
import com.jxai.lib.core.config.BaseAppConfig

class AppConfig : BaseAppConfig() {

    override fun onCreate() {
        isDebug = BuildConfig.DEBUG
        super.onCreate()
    }

}