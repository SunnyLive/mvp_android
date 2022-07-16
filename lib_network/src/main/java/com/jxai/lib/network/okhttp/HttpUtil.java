package com.jxai.lib.network.okhttp;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jxai.lib.network.BuildConfig;
import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls().create();
        return new Retrofit.Builder().client(provideOkHttpClient()).baseUrl(mBaseURL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
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
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
            Log.i("OkHttpClient ",message);
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .sslSocketFactory(new NetworkSSL(TrustManager.trustAllCert), TrustManager.trustAllCert)
                .connectTimeout(60* 1000, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .readTimeout(60 * 1000, TimeUnit.SECONDS)
                .writeTimeout(60 * 1000, TimeUnit.SECONDS)
                .build();
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
