package com.jxai.lib.core.mvp;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class BasePresenter<T extends IMvpView> implements IPresenter<T> {


    Gson gson;
    protected CompositeDisposable compositeDisposable;

    public BasePresenter() {
        gson = new Gson();
    }


    protected Reference<T> mViewRef;

    @Override
    public void attachView(T mvpView) {
        mViewRef = new WeakReference<T>(mvpView);
        compositeDisposable = new CompositeDisposable();
    }

    public void addNet(Disposable d) {
        if (null == compositeDisposable) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(d);
    }

    /**
     * 通过getView获取mView 可以有效避免context内存泄露
     *
     * @return
     */
    protected T getView() {
        return mViewRef.get();
    }

    @Override
    public void detachView() {
        clearHttpRequest();
    }

    public void clearHttpRequest() {
        if (null != compositeDisposable && compositeDisposable.size() > 0) {
            compositeDisposable.clear();
        }
    }

    public boolean isViewAttached() {
        return getView() != null;
    }

    public <T> FlowableTransformer<T, T> io_main() {
        return upstream ->
                upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
    }

    public <T> FlowableTransformer<HttpResult<T>, T> handleResult() {
        return upstream -> upstream.flatMap((Function<HttpResult<T>, Flowable<T>>) tHttpResult -> {
            if (tHttpResult.isSuccess()) {
                return createData(tHttpResult.data);
            } else {
                String des = "网络出错，请稍后重试！";
                return Flowable.error(new Throwable(des));
            }
        }).compose(io_main());
    }


    private <T> Flowable<T> createData(final T t) {
        return Flowable.create(subscriber -> {
            try {
                subscriber.onNext(t);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }, BackpressureStrategy.ERROR);
    }

    public RequestBody createRequestBody(Map map) {
        String json = gson.toJson(map);
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));
        return requestBody;
    }

    public class ParamsBuilder {
        private Map<String, String> params = new HashMap<>();

        public ParamsBuilder put(String key, String value) {
            params.put(key, value);
            return this;
        }

        public RequestBody build() {
            return createRequestBody(params);
        }
    }

    public RequestBody createParamsRequestBody(Map map) {
        Map params = new HashMap();
        params.put("data", map);
        RequestBody requestBody = RequestBody.create(gson.toJson(params), MediaType.parse("application/json"));
        return requestBody;
    }


}