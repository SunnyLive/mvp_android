package com.jxai.module.camera.mvp

import android.util.Log
import com.jxai.lib.network.okhttp.HttpUtil
import com.jxai.lib.core.mvp.BasePresenter
import com.orhanobut.logger.Logger

class CameraPresenter : BasePresenter<ICameraView>() {

    private val mCameraApi by lazy {
        HttpUtil.builder().setHttpURL("http://www.baidu.com").build().createApi(CameraApi::class.java)
    }

    fun requestServerVerify() {
        val params = HashMap<String, String>()
        //params.put("image","sss")
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
                Log.e("ms", "_____"+it)
            })
        )


    }


}