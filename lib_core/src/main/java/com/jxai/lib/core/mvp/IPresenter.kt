package com.jxai.lib.core.mvp

interface IPresenter<V : IMvpView?> {
    fun attachView(mvpView: V)
    fun detachView()
}