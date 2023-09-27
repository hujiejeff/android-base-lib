package com.hujiejeff.library.base.network.interceptor

import android.util.Log
import com.google.gson.Gson
import com.szpgm.commonlib.network.entity.ErrorType
import com.szpgm.commonlib.network.entity.ResponseBean
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.UnknownHostException

/**
 * 标准化网络请求输出，提前控制网络状态，输入到标准Bean中
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
                    response
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
}