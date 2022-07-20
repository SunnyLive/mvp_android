package com.jxai.module.camera.mvp

import com.jxai.lib.core.mvp.IMvpView
import com.jxai.module.camera.mvp.dto.PictureVerify

interface ICameraView:IMvpView{

    fun upLoadImageInfo(imageInfo: PictureVerify)

    fun onTakePicture()
}