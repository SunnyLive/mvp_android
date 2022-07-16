package com.jxai.lib.core.ui

import com.jxai.lib.core.mvp.HttpResult
import com.jxai.lib.core.mvp.IMvpView
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class BaseRxActivity : RxAppCompatActivity() ,IMvpView{
    open fun <T> io_main(): FlowableTransformer<T, T>? {
        return FlowableTransformer { upstream: Flowable<T> ->
            upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    open fun <T> handleResult(): FlowableTransformer<HttpResult<T>?, T>? {
        return FlowableTransformer { upstream ->
            upstream.flatMap { tHttpResult ->
                if (tHttpResult.isSuccess) {
                    createData(tHttpResult.data)
                } else if (tHttpResult.isTokenInvalid) {
                    Flowable.error(Throwable(tHttpResult.description))
                } else {
                    Flowable.error(Throwable(tHttpResult.description))
                }
            }.compose(io_main())
        }
    }

    private fun <T> createData(t: T): Flowable<T>? {
        return Flowable.create({ subscriber: FlowableEmitter<T> ->
            try {
                subscriber.onNext(t)
                subscriber.onComplete()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }, BackpressureStrategy.ERROR)
    }
}