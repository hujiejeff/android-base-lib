package com.hujiejeff.library.base.network.example

import androidx.lifecycle.LiveData
import com.hujiejeff.library.base.network.entity.ResponseBean
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.http.GET

interface ExampleApi {
    /**
     * 首页banner
     */
    @GET("/banner/json")
    suspend fun getBanners(): ResponseBean<ExampleRespBean<List<BannerBean>>>


    @GET("/banner/json")
    fun getBannersByFlow(): Flow<ResponseBean<ExampleRespBean<List<BannerBean>>>>

    /**
     * 捕捉其他异常测试, 这里测试json转换异常
     */
    @GET("/banner/json")
    fun getBannersByFlowCatchException(): Flow<ResponseBean<ExampleRespBean<BannerBean>>>


    @GET("/banner/json")
    fun getBannersByLiveData(): LiveData<ResponseBean<ExampleRespBean<List<BannerBean>>>>


    @GET("/banner/json")
    fun getBannerByCall(): Call<ResponseBean<ExampleRespBean<List<BannerBean>>>>
}