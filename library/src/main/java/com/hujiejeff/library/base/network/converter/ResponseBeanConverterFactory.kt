package com.hujiejeff.library.base.network.converter

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

/**
 * 包装Gson转换器，转换出统一ResponseBean
 */
class ResponseBeanConverterFactory : Converter.Factory() {
    private var gsonConverterFactory: GsonConverterFactory = GsonConverterFactory.create()

    companion object {
        fun create() = ResponseBeanConverterFactory()
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val gsonResponseBodyConverter =
            gsonConverterFactory.responseBodyConverter(type, annotations, retrofit)
        return ResponseBeanResponseBodyConverter(gsonResponseBodyConverter as Converter<ResponseBody, *>)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        return gsonConverterFactory.requestBodyConverter(
            type,
            parameterAnnotations,
            methodAnnotations,
            retrofit
        )
    }
}