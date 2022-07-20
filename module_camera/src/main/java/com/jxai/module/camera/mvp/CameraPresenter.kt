package com.jxai.module.camera.mvp

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.google.gson.Gson
import com.jxai.lib.network.okhttp.HttpUtil
import com.jxai.lib.core.mvp.BasePresenter
import com.jxai.lib.picture.zip.PictureUtil
import com.jxai.lib.utils.encry.Base64Util
import com.jxai.module.camera.mvp.dto.PictureVerify
import com.jxai.module.camera.ui.CameraActivity
import com.orhanobut.logger.Logger
import java.lang.ref.WeakReference

class CameraPresenter : BasePresenter<ICameraView>() {

    private val mUiHandler by lazy {
        UIHandler(WeakReference(this@CameraPresenter))
    }

    private val mCameraApi by lazy {
        HttpUtil.builder().setHttpURL("http://114.132.239.184:5000/").build().createApi(CameraApi::class.java)
    }

    init {
        mUiHandler.sendMessageDelayed(Message(),10 * 1000)
    }


    /**
     *
     * 这里是请求服务端ai验证图片
     *
     * [sourceImg] 图片的地址
     *
     */
    fun requestServerVerify(sourceImg:String) {

        val params = HashMap<String, String>()
        params["image"] = sourceImg
        view?.showLoading()
        addNet(
            mCameraApi.requestServerVerify(createRequestBody(params)).compose(io_main()).subscribe({

                view?.apply {
                    hideLoading()
                    upLoadImageInfo(it)
                }

            }, {
                view?.apply {
                    hideLoading()
                    netError()
                }
                Log.e("ms", "_____$it")
            })
        )


    }

    /**
     *
     * 这里开始计时拍照
     * 目前是每10秒钟拍照一次
     *
     */
    class UIHandler(private val weak: WeakReference<CameraPresenter>) :
        Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            weak.get()?.apply {
                view?.onTakePicture()
                mUiHandler.sendMessageDelayed(Message(),10 * 1000)
            }
        }
    }
}