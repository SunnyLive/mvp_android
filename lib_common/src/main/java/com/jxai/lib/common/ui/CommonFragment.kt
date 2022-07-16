package com.jxai.lib.common.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jxai.lib.core.mvp.BasePresenter
import com.jxai.lib.core.mvp.IMvpView
import com.jxai.lib.core.ui.BaseRxFragment

abstract class CommonFragment<V : IMvpView, P : BasePresenter<V>> : BaseRxFragment(){


    var p: P? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        p = initPresenter()
        p?.apply {
            attachView(this as V)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        p?.apply {
            detachView()
        }
    }

    protected abstract fun initPresenter(): P

}