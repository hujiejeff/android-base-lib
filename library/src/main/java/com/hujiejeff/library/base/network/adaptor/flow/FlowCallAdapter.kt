package com.hujiejeff.library.base.network.adaptor.flow

import android.util.Log
import com.hujiejeff.library.base.network.entity.ErrorType
import com.hujiejeff.library.base.network.entity.ResponseBean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.awaitResponse
import java.lang.reflect.Type


/**
 * 输出flow冷流，通过hoFlow = coldFlow.stateIn转换热流, 并且捕捉其他异常
 *
 */
class FlowCallAdapter<T>(private val responseType: Type) : CallAdapter<T, Flow<T>> {
    override fun responseType(): Type = responseType
    override fun adapt(call: Call<T>): Flow<T> {
        return flow {
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