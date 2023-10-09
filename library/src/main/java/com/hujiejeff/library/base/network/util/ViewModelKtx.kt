package com.hujiejeff.library.base.network.util

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.hujiejeff.library.base.network.entity.ResponseBean
import com.hujiejeff.library.base.network.entity.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * 触发流转成请求数据流
 */
fun <U, T> Flow<U>.asRequestFlow(
    delayTime: Long = 0,
    dataFlow: (U) -> Flow<ResponseBean<T>>,
): Flow<RequestState<T>> {
    return transform { upperValue ->
        emit(RequestState.Loading("loading"))
        delay(delayTime)
        emitAll(dataFlow(upperValue).map { responseBean ->
            if (responseBean.isSuccess() && responseBean.response != null) {
                RequestState.Success(responseBean.response)
            } else {
                RequestState.Failed(responseBean.errorMsg)
            }
        })
    }
}

/**
 * 请求数据流转成UI状态热流
 */
fun <T> ViewModel.asUiStateFlow(requestFlow: Flow<T>, firstState: T) =
    requestFlow.stateIn(
        scope = viewModelScope,
        initialValue = firstState,
        started = SharingStarted.WhileSubscribed(5_000)
    )

/**
 * 收集
 */
fun LifecycleOwner.collectStateFlow(
    state: Lifecycle.State,
    block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            block()
        }
    }
}

/**
 * Flow请求数据流状态
 */
sealed class RequestState<T> {
    class Success<T>(val data: T) : RequestState<T>()
    class Loading<T>(val msg: String) : RequestState<T>()
    class Failed<T>(val msg: String) : RequestState<T>()
}

/**
 * dsl方式协程请求调用
 */
inline fun <T> ViewModel.singleRequest(
    crossinline dsl: suspend HttpRequestDsl<T>.() -> Unit,
){
    val httpRequestDsl = HttpRequestDsl<T>()
    viewModelScope.launch {
        httpRequestDsl.dsl()
        withContext(Dispatchers.Main) {
            httpRequestDsl.onStart?.invoke()
        }
        runCatching {
            withContext(Dispatchers.IO) {
                httpRequestDsl.onRequest?.invoke() ?: throw Exception("data is null")
            }
        }.onSuccess {response ->
            if (response is ResponseBean<*>) {
                (response as ResponseBean<*>).let {
                    if (it.isSuccess() && it.response != null) {
                        httpRequestDsl.onSuccess?.invoke(it as T)
                    } else {
                        httpRequestDsl.onFailed?.invoke(Exception(it.errorMsg))
                    }
                }
            } else {
                httpRequestDsl.onSuccess?.invoke(response as T)
            }

        }.onFailure {
            httpRequestDsl.onFailed?.invoke(it)
        }
    }
}


/**
 * 原始请求
 */
/*inline fun <T> ViewModel.originalRequest(
    crossinline api: () -> Call<ResponseBean<T>>,
    crossinline onStart: () -> Unit = {},
    crossinline onSuccess: (T) -> Unit = {},
    crossinline onFailed: (Throwable) -> Unit = {}
) {
    onStart.invoke()
    api().enqueue(object : Callback<ResponseBean<T>> {
        override fun onResponse(call: Call<ResponseBean<T>>, response: Response<ResponseBean<T>>) {
            val responseBean = response.body()
            if (responseBean != null && responseBean.isSuccess() && responseBean.response != null) {
                onSuccess.invoke(responseBean.response!!)
            } else {
                onFailed.invoke(Exception(responseBean?.errorMsg?: "null data"))
            }
        }

        override fun onFailure(call: Call<ResponseBean<T>>, t: Throwable) {
            onFailed.invoke(t)
        }
    })
}*/

inline fun <T> ViewModel.originalRequestDsl(
    crossinline dsl: HttpRequestDsl<T>.() -> Unit,
) {
    val httpRequestDsl = HttpRequestDsl<T>()
    httpRequestDsl.dsl()
    httpRequestDsl.onStart?.invoke()
    httpRequestDsl.onRequestForCall?.invoke()?.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            runCatching {
                response.body() ?: throw Exception("data is null")
            }.onSuccess {responseBean ->
                if (responseBean is ResponseBean<*>) {
                    if (responseBean.isSuccess() && responseBean.response != null) {
                        httpRequestDsl.onSuccess?.invoke((responseBean))
                    } else {
                        httpRequestDsl.onFailed?.invoke(Exception(responseBean.errorMsg))
                    }
                } else {
                    httpRequestDsl.onSuccess?.invoke(responseBean)
                }
            }.onFailure {
                httpRequestDsl.onFailed?.invoke(it)
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            httpRequestDsl.onFailed?.invoke(t)
        }
    })
}


/**
 * DSL和链式调用
 */
class HttpRequestDsl<T> {
    var onSuccess: ((T) -> Unit)? = null
    var onFailed: ((Throwable) -> Unit)? = null
    var onStart: (() -> Unit)? = null
    var onRequest: (suspend  () -> T)? = null
    var onRequestForCall: (() -> Call<T>)? = null

    fun onSuccess(onSuccess: ((T) -> Unit)?): HttpRequestDsl<T> {
        this.onSuccess = onSuccess
        return this
    }

    fun onFailed(onFailed: ((Throwable) -> Unit)?): HttpRequestDsl<T> {
        this.onFailed = onFailed
        return this
    }

    fun onStart(onStart: (() -> Unit)?): HttpRequestDsl<T> {
        this.onStart = onStart
        return this
    }

    fun onRequest(onRequest: (suspend () -> T)?): HttpRequestDsl<T> {
        this.onRequest = onRequest
        return this
    }

    fun onRequestForCall(onRequestForCall: (() -> Call<T>)?):HttpRequestDsl<T> {
        this.onRequestForCall = onRequestForCall
        return this;
    }

}