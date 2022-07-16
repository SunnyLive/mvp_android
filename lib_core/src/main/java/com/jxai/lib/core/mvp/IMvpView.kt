package com.jxai.lib.core.mvp

interface IMvpView {
    fun showLoading()

    fun isLoading(): Boolean

    fun hideLoading()

    fun toastTip(msgId: Int)

    fun toastTip(msg: CharSequence?)

    fun netError()

    fun tokenIInvalid()

    fun jump()

    fun setProgressPercent(precent: String?)

}