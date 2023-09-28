package com.szpgm.commonlib.network

import android.util.Log
import com.hujiejeff.library.base.network.interceptor.NetStateInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


abstract class HttpAbstract {
    companion object {
        const val TAG = "HttpAbstract"
    }

    private lateinit var retrofit: Retrofit
    private val apiMaps = mutableMapOf<Class<*>, Any>()

    private fun getRetrofit(): Retrofit {
        if (!this::retrofit.isInitialized) {
            buildRetrofitClient()
        }
        return retrofit
    }

    inline fun <reified T> getApi(): T {
        val clazz = T::class.java
        return getApi(clazz)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getApi(clazz: Class<T>): T {
        if (!apiMaps.contains(clazz)) {
            val api = getRetrofit().create(clazz)
            apiMaps[clazz] = api!!
        }
        return apiMaps[clazz] as T
    }

    private fun buildOKHttpClient(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
        val interceptors = getInterceptors()
        for (interceptor in interceptors) {
            builder.addInterceptor(interceptor)
        }
        if (useStandardResponseBean) {
            builder.addInterceptor(NetStateInterceptor())
        }
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        builder.addInterceptor(logger)
        return configureBaseClient(builder).build()
    }

    private fun buildRetrofitClient() {
        if (baseUrl.isBlank()) {
            throw Exception("BaseUrl is Empty, you must set it")
        }
        retrofit = Retrofit.Builder()
            .client(buildOKHttpClient())
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .also { retrofitBuilder ->
                getCallAdapterFactory()?.let { retrofitBuilder.addCallAdapterFactory(it) }
                configureRetrofit(retrofitBuilder)
            }
            .build()
    }

    protected val TIME_OUT = 30L
    protected open fun getInterceptors(): List<Interceptor> {
        return emptyList()
    }

    protected open fun getCallAdapterFactory(): CallAdapter.Factory? {
        return null
    }

    abstract val baseUrl: String

    /**
     * 是否使用标准响应Bean
     */
    abstract val useStandardResponseBean: Boolean

    protected open fun configureBaseClient(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        Log.d(TAG, "Customize OKHttpClient")
        return builder
    }

    protected open fun configureRetrofit(builder: Retrofit.Builder): Retrofit.Builder {
        Log.d(TAG, "Customize Retrofit")
        return builder
    }

}