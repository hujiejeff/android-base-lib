package com.hujiejeff.library.base.network.demo

import com.szpgm.commonlib.network.entity.ResponseBean
import retrofit2.http.GET

interface SimpleApi {
    /**
     * 首页banner
     */
    @GET("/banner/json")
    suspend fun getBanners(): ResponseBean<Any>
}