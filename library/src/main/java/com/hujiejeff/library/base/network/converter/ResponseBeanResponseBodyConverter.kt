package com.hujiejeff.library.base.network.converter

import com.szpgm.commonlib.network.entity.ResponseBean
import okhttp3.ResponseBody
import retrofit2.Converter


class ResponseBeanResponseBodyConverter<T>(private val gsonResponseBodyConverter: Converter<ResponseBody, T>) :
    Converter<ResponseBody, ResponseBean<T>> {
    override fun convert(value: ResponseBody): ResponseBean<T>{
        val t = gsonResponseBodyConverter.convert(value)
        if (t is ResponseBean<*>) {
            return t as ResponseBean<T>
        } else {
            return ResponseBean(t)
        }
    }
}