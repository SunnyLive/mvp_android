package com.jxai.module.camera.mvp

import com.jxai.module.camera.mvp.dto.PictureVerify
import io.reactivex.Flowable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface CameraApi {


    @POST("api/detect")
    fun requestServerVerify(@Body body: RequestBody): Flowable<PictureVerify>


}