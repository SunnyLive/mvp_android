package com.jxai.lib.common.mvp

import com.jxai.lib.network.okhttp.HttpUtil
import com.jxai.lib.core.mvp.BasePresenter
import com.orhanobut.logger.Logger

class CommonPresenter : BasePresenter<ICommonView>() {

    val mCommonApi by lazy { HttpUtil.builder()
        .isDeBug(true)
        .setHttpURL("www.baidu.com").build()
        .createApi(CommonApi::class.java) }

    override fun attachView(mvpView: ICommonView?) {
        super.attachView(mvpView)
    }


    fun getRequestIVerify(){

        val mParams = HashMap<String,String>()

        addNet(mCommonApi.requestServer(createRequestBody(mParams)).compose(io_main()).subscribe({
            if (it.isSuccess) {
                Logger.i("")
            }
        },{
            Logger.e("",it.message)
        }))

    }


}