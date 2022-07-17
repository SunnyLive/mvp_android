package com.jxai.module.camera.mvp

import com.jxai.lib.core.mvp.HttpResult
import io.reactivex.Flowable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface CameraApi {


    @POST("/")
    fun requestServerVerify(@Body body: RequestBody): Flowable<HttpResult<String>>


}