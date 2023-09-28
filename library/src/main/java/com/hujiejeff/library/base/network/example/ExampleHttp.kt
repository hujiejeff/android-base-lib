package com.hujiejeff.library.base.network.example

import android.util.Log
import com.szpgm.commonlib.network.HttpAbstract
import com.hujiejeff.library.base.network.adaptor.flow.FlowCallAdapterFactory
import com.hujiejeff.library.base.network.interceptor.NetStateInterceptor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import retrofit2.CallAdapter


object ExampleHttp: HttpAbstract() {
    override val baseUrl: String = "https://www.wanandroid.com"
    override val useStandardResponseBean: Boolean = false

    /**
     * 协程方式调用
     */
    fun exampleRequestByCoroutines() {
        val exampleApi: ExampleApi = getApi()
        runBlocking {
            kotlin.runCatching {
                exampleApi.getBanners()
            }.onFailure {
                Log.d("Test","" + it)
            }.onSuccess {
                Log.d("Test","" + it)
            }
        }
    }

    fun exampleByFlow() {
        val exampleApi: ExampleApi = getApi()
        runBlocking {
            exampleApi.getBannersByFlow().collectLatest {
                Log.d("Test", "By Flow Response" + it)
            }
        }
    }

    fun exampleByFlowCatchException() {
        val exampleApi: ExampleApi = getApi()
        runBlocking {
            exampleApi.getBannersByFlowCatchException().collectLatest {
                Log.d("Test", "By Flow Response" + it)
            }
        }
    }

    fun exampleByLiveData() {

    }

    fun exampleByCall() {

    }

    fun exampleByRxJava() {

    }

    override fun getInterceptors(): List<Interceptor> {
        return listOf(NetStateInterceptor())
    }

    override fun getCallAdapterFactory(): CallAdapter.Factory {
        return FlowCallAdapterFactory()
    }
}