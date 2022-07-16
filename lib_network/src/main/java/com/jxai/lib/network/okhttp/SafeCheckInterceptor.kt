package com.jxai.lib.network.okhttp

import android.text.TextUtils
import com.orhanobut.logger.Logger
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


/**
 * 统一验签
 *
 * @Author gukaihong
 * @Time 2020/12/25
 */
class SafeCheckInterceptor : Interceptor {
    companion object {
        const val SALT = "ilgb%S3U74KLN0w\$"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val reqBody = request.body
        if (TextUtils.equals(request.method, "POST")
            || TextUtils.equals(request.method, "PUT")
            || TextUtils.equals(request.method, "DELETE")) {
            var body = ""
            if (reqBody != null) {
                body = getRequestBody(reqBody)
                Logger.i("gkh", "body = $body")
            }
            if (TextUtils.isEmpty(body)) {
                body = "{}"
                //如果body为空，需要有内容，才能过校验
                requestBuilder.method(request.method, body.toRequestBody("application/json".toMediaTypeOrNull()))
            }
            val guid = GUIDUtils.getGUID()
            val time = System.currentTimeMillis().toString()
            val key = StringFormatUtils.string2Md5(SALT + time + guid + body + SALT)
            requestBuilder
                    .addHeader("X-SIGN-KEY", key)
                    .addHeader("X-SIGN-TIMESTAMP", time)
                    .addHeader("X-SIGN-NONCE", guid)
        }

//        requestBuilder
//                .addHeader("X-MAC", "")
//                .addHeader("X-VERSION", "")
//                .addHeader("X-SOURCE-ID", "")
//                .addHeader("X-PHONE-TYPE", "")
//                .addHeader("X-SYSTEM-VERSION", "")
//                .addHeader("X-TERMINAL-TYPE", "android")

        return chain.proceed(requestBuilder.build())
    }


    /**
     * 获取RequestBody中数据
     */
    private fun getRequestBody(requestBody: RequestBody): String {
        val buffer = Buffer()
        try {
            requestBody.writeTo(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
        //编码设为UTF-8
        var charset: Charset? = StandardCharsets.UTF_8
        val contentType: MediaType? = requestBody.contentType()
        if (contentType != null) {
            charset = contentType.charset(StandardCharsets.UTF_8)
        }
        return if (charset == null) {
            ""
        } else buffer.readString(charset)
    }

}