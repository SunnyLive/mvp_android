package com.jxai.lib.common.http;

import com.jxai.lib.common.BuildConfig;
import com.jxai.lib.network.okhttp.FileRequestBodyConverterFactory;
import com.jxai.lib.network.okhttp.NetworkSSL;
import com.jxai.lib.network.okhttp.SafeCheckInterceptor;
import com.jxai.lib.network.okhttp.TrustManager;
import com.orhanobut.logger.Logger;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpUtil {

    private String mBaseURL = "www.baidu.com";
    private boolean isDeBug = BuildConfig.DEBUG;
    private static Retrofit mHttpClient;

    public HttpUtil(Builder builder) {
        this.isDeBug = builder.isDeBug;
        this.mBaseURL = builder.mBaseURL;
    }


    private Retrofit initClient(){
        return new Retrofit.Builder().client(provideOkHttpClient()).baseUrl(mBaseURL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(new FileRequestBodyConverterFactory())
                .build();
    }


    public synchronized <T> T createApi(Class<T> clazz) {
        if (mHttpClient == null) {
            mHttpClient = initClient();
        }
        T t = mHttpClient.create(clazz);
        return t;
    }


    /**
     * 这里配置okhttp
     *
     * @return OkHttpClient
     */
    private OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60 * 1000, TimeUnit.MILLISECONDS).readTimeout(60 * 1000, TimeUnit.MILLISECONDS);
        builder.addInterceptor(chain -> {
            Request request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    //.addHeader("Authorization", "Bearer")
                    .build();
            return chain.proceed(request);
        });
        //添加统一验证
        builder.addInterceptor(new SafeCheckInterceptor());
        if (isDeBug) {
            builder.addInterceptor(logInterceptor());
            builder.sslSocketFactory(new NetworkSSL(TrustManager.trustAllCert),
                    TrustManager.trustAllCert);//屏蔽ssl整数验证
        }
        return builder.build();
    }



    private HttpLoggingInterceptor logInterceptor() {
        //新建log拦截器
        HttpLoggingInterceptor interceptor =
                new HttpLoggingInterceptor(message -> {
                    Logger.i("OkHttpClient " + message);
                });
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }


    public static Builder builder() {
        return new Builder();
    }

   public static class Builder {

        private String mBaseURL;
        private boolean isDeBug = BuildConfig.DEBUG;

        public Builder isDeBug(boolean isDebug) {
            this.isDeBug = isDebug;
            return this;
        }

        public Builder setHttpURL(String url) {
            this.mBaseURL = url;
            return this;
        }

        public HttpUtil build() {
            return new HttpUtil(this);
        }

    }

}
