package com.jxai.lib.common.mvp

import com.jxai.lib.core.mvp.HttpResult
import io.reactivex.Flowable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface CommonApi {

    @POST("")
    fun requestServer(@Body body: RequestBody): Flowable<HttpResult<*>>
}