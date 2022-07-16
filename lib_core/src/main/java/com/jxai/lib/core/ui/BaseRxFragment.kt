package com.jxai.lib.core.ui

import android.util.Log
import com.jxai.lib.core.mvp.HttpResult
import com.jxai.lib.core.mvp.IMvpView
import com.trello.rxlifecycle2.components.support.RxFragment
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers


abstract class BaseRxFragment: RxFragment(), IMvpView {

    fun <T> io_main(): FlowableTransformer<T, T>? {
        return FlowableTransformer { upstream: Flowable<T> ->
            upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> handleResult(): FlowableTransformer<HttpResult<T>?, T>? {
        return FlowableTransformer { upstream ->
            upstream.flatMap(Function { tHttpResult ->
                if (tHttpResult.isSuccess()) {
                    createData<T>(tHttpResult.data)
                } else {
                    if (tHttpResult.isTokenInvalid()) {
                        tokenIInvalid()
                        Flowable.error<T>(Throwable(tHttpResult.description))
                    } else {
                        Flowable.error<T>(Throwable(tHttpResult.description))
                    }
                }
            }).compose(io_main())
        }
    }

    private fun <T> createData(t: T): Flowable<T>? {
        return Flowable.create({ subscriber: FlowableEmitter<T> ->
            try {
                subscriber.onNext(t)
                subscriber.onComplete()
            } catch (e: Exception) {
                subscriber.onError(e)
                Log.e("",e.message + "")
            }
        }, BackpressureStrategy.ERROR)
    }



}