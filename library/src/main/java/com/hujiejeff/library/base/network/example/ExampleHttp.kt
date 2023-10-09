package com.hujiejeff.library.base.network.example

import android.util.Log
import androidx.lifecycle.asFlow
import com.hujiejeff.library.base.network.HttpAbstract
import com.hujiejeff.library.base.network.adaptor.ComplexAdapterFactory
import com.hujiejeff.library.base.network.entity.ResponseBean
import com.hujiejeff.library.base.network.interceptor.NetStateInterceptor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response


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
        val exampleApi: ExampleApi = getApi()
        runBlocking {
            runBlocking {
                exampleApi.getBannersByLiveData().asFlow().collectLatest {
                    Log.d("Test", "By Flow Response" + it)
                }
            }
        }
    }

    fun exampleByCall() {
        val exampleApi: ExampleApi = getApi()
        exampleApi.getBannerByCall().enqueue(object : Callback<ResponseBean<ExampleRespBean<List<BannerBean>>>>{
            override fun onResponse(
                call: Call<ResponseBean<ExampleRespBean<List<BannerBean>>>>,
                response: Response<ResponseBean<ExampleRespBean<List<BannerBean>>>>
            ) {
                Log.d("Test", "By Flow Response" + response.body())
            }

            override fun onFailure(
                call: Call<ResponseBean<ExampleRespBean<List<BannerBean>>>>,
                t: Throwable
            ) {
                Log.d("Test", "By Flow Response" + t.toString())
            }

        })
    }

    fun exampleByRxJava() {

    }

    override fun getInterceptors(): List<Interceptor> {
        return listOf(NetStateInterceptor())
    }

    override fun getCallAdapterFactory(): CallAdapter.Factory {
        return ComplexAdapterFactory()
    }
}