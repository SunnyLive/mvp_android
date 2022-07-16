package com.jxai.module.camera.mvp

import com.jxai.lib.common.http.HttpUtil
import com.jxai.lib.core.mvp.BasePresenter
import com.orhanobut.logger.Logger

class CameraPresenter : BasePresenter<ICameraView>() {

    private val mCameraApi by lazy {
        HttpUtil.builder().setHttpURL("").isDeBug(true).build().createApi(CameraApi::class.java)
    }

    fun requestServerVerify() {
        val params = HashMap<String, String>();
        view?.showLoading()
        addNet(
            mCameraApi.requestServerVerify(createRequestBody(params)).compose(io_main()).subscribe({
                view?.hideLoading()
                Logger.d(it)
            }, {
                view?.apply {
                    hideLoading()
                    netError()
                }
                Logger.e("ms", it)
            })
        )


    }


}