package com.hujiejeff.library.base.network.adaptor.beanchange

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class BeanChangeCallAdapter<T>(private val responseType: Type): CallAdapter<T, Flow<T>> {
    override fun responseType(): Type = responseType

    override fun adapt(call: Call<T>): Flow<T> {
        return flow {  }
    }
}