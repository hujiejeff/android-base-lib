package com.hujiejeff.library.base.network.adaptor

import androidx.lifecycle.LiveData
import com.hujiejeff.library.base.network.adaptor.flow.FlowCallAdapter
import com.hujiejeff.library.base.network.adaptor.livedata.LiveDataCallAdapter
import com.hujiejeff.library.base.network.entity.ResponseBean

import kotlinx.coroutines.flow.Flow
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ComplexAdapterFactory: CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val callType = getRawType(returnType)
        //获取第一个泛型类型
        val observableType = getParameterUpperBound(0, returnType as ParameterizedType)
        val rawType = getRawType(observableType)
        if (rawType != ResponseBean::class.java) {
            throw IllegalArgumentException("type must be ResponseBean")
        }

        if (observableType !is ParameterizedType) {
            throw IllegalArgumentException("resource must be parameterized")
        }

        return when(callType) {
            LiveData::class.java -> {
                LiveDataCallAdapter<Any>(observableType)
            }

            Flow::class.java -> {
                FlowCallAdapter<Any>(observableType)
            }
            else -> null
        }
    }
}