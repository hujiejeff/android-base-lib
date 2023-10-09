package com.hujiejeff.library.base.network.adaptor.livedata

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData

import com.hujiejeff.library.base.network.entity.ErrorType
import com.hujiejeff.library.base.network.entity.ResponseBean
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.awaitResponse
import java.lang.reflect.Type


/**
 * 输出LiveData, 并且捕捉其他异常
 *
 */
class LiveDataCallAdapter<T>(private val responseType: Type) : CallAdapter<T, LiveData<T>> {
    override fun responseType(): Type = responseType
    override fun adapt(call: Call<T>): LiveData<T> {
        return liveData {
            runCatching {
                call.awaitResponse()
            }.onFailure { e ->
                Log.d("FlowCallAdapter", "error: $e")
                val error =
                    ResponseBean(null, ErrorType.OTHER_EXCEPTION, e.message ?: e.toString())
                emit(error as T)
            }.onSuccess {
                emit(it.body()!!)
            }
        }
    }
}