package com.jxai.lib.common.ui

import android.os.Bundle
import android.os.PersistableBundle
import com.alibaba.android.arouter.launcher.ARouter
import com.jxai.lib.core.mvp.BasePresenter
import com.jxai.lib.core.mvp.IMvpView
import com.jxai.lib.core.ui.BaseRxActivity
import io.reactivex.disposables.CompositeDisposable

abstract class CommonActivity<V : IMvpView, P : BasePresenter<V>> : BaseRxActivity() {
    var netManager: CompositeDisposable? = null
    var p: P? = null


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        ARouter.getInstance().inject(this)
        p = initPresenter()
        netManager = CompositeDisposable()
        p?.apply {
            attachView(this as V)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        p?.apply {
            detachView()
            p = null
        }
        netManager?.clear()

    }

    abstract fun initPresenter(): P

    abstract fun initView()

    abstract fun initEvent()


}