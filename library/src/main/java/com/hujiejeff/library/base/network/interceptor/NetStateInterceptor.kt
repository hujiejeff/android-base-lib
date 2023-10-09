package com.hujiejeff.library.base.network.interceptor

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.hujiejeff.library.base.network.entity.ErrorType
import com.hujiejeff.library.base.network.entity.ResponseBean
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.GzipSource
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * 标准化网络请求输出，提前控制网络状态，输入到标准Bean中，提前捕捉网络错误等异常
 */
class NetStateInterceptor : Interceptor {
    private val gson = Gson()
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("NetStateInterceptor", "NetStateInterceptor Start")
        val request = chain.request()
        var response: Response? = null
        val errorBean: ResponseBean<*>
        try {
            response = chain.proceed(request)
        } catch (e: UnknownHostException) {
            Log.d("NetStateInterceptor", "UnknownHostException")
        }
        response = if (response == null) {
            //网络丢失
            buildErrorResponse(request, null, ErrorType.UN_HOST, errorMsg)
        } else {
            //状态码不正常
            if (response.code !in 200..299) {
                buildErrorResponse(request, response, ErrorType.NOT_FOUND, errorMsg)
            } else {
                if (response.body == null) {
                    //无数据
                    buildErrorResponse(request, response, ErrorType.SERVER_ERROR, errorMsg)
                } else {
                    buildSuccessResponse(response)
                }
            }
        }
        Log.d("NetStateInterceptor", "NetStateInterceptor End")
        return response
    }

    private fun buildErrorResponse(
        request: Request,
        response: Response?,
        errorType: ErrorType,
        errorMsg: String
    ): Response {
        val newResponse: Response
        val errorBean: ResponseBean<*>
        errorBean = ResponseBean(null, errorType, errorMsg)
        val errorBody = gson.toJson(errorBean).toResponseBody()
        newResponse = if (response == null && errorType == ErrorType.UN_HOST) {
            Response.Builder().request(request).protocol(Protocol.HTTP_2).message("").code(200)
                .body(errorBody).build()
        } else {
            response!!.newBuilder().code(200).body(errorBody).build()
        }

        return newResponse
    }

    protected val errorMsg = "NetWork Error"

    private fun buildSuccessResponse(response: Response): Response {
        val headers = response.headers
        val responseBody = response.body
        if (responseBody == null) {
            return response
        } else {
            val contentLength = responseBody.contentLength()
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer
            var gzippedLength: Long? = null
            if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                gzippedLength = buffer.size
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }

            val contentType = responseBody.contentType()
            val charset: Charset =
                contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            var responseStr = ""
            if (contentLength != 0L) {
                responseStr = buffer.clone().readString(charset)
                Log.d("NetStateInterceptor", "buildSuccessResponse: " + responseStr)
            }

            //拼装出正确的标准响应json
            val jsonObject = gson.fromJson(responseStr, JsonObject::class.java).let { oldJsonObj ->
                JsonObject().apply {
                    add("response", oldJsonObj)
                    add("errorCode", gson.toJsonTree(ErrorType.SUCCESS))
                    addProperty("errorMsg", System.currentTimeMillis().toString())
                }
            }
            Log.d("NetStateInterceptor", "buildSuccessResponse: $jsonObject")
            return response.newBuilder().body(jsonObject.toString().toResponseBody()).build()
        }
    }
}