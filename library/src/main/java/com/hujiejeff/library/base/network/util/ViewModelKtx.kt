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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 触发流转成请求数据流
 */
fun <U, T> Flow<U>.asRequestFlow(
    dataFlow: (U) -> Flow<ResponseBean<T>>,
): Flow<RequestState<T>> {
    return transform { upperValue ->
        Log.d("hujie", "requestAsStateFlow: ")
        delay(1000L)
        emit(RequestState.Loading("loading"))
        delay(1000L)
        emitAll(dataFlow(upperValue).map { responseBean ->
            if (responseBean.isSuccess() && responseBean.response != null) {
                RequestState.Success(responseBean.response!!)
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


sealed class RequestState<T> {
    class Success<T>(val data: T) : RequestState<T>()
    class Loading<T>(val msg: String) : RequestState<T>()
    class Failed<T>(val msg: String) : RequestState<T>()
}

inline fun <T> ViewModel.singleRequest(
    crossinline api: suspend () -> ResponseBean<T>,
    crossinline onStart: () -> Unit = {},
    crossinline onSuccess: (T) -> Unit = {},
    crossinline onFailed: (String) -> Unit = {}
) = viewModelScope.launch {
    onStart.invoke()
    runCatching {
        api()
    }.onSuccess {
        if (it.isSuccess() && it.response != null) {
            onSuccess.invoke(it.response!!)
        } else {
            onFailed.invoke(it.errorMsg)
        }
    }.onFailure {
        onFailed.invoke(it.toString())
    }
}

/**
 * 原始请求
 */
inline fun <T> ViewModel.originalRequest(
    crossinline api: () -> Call<ResponseBean<T>>,
    crossinline onStart: () -> Unit = {},
    crossinline onSuccess: (T) -> Unit = {},
    crossinline onFailed: (String) -> Unit = {}
) {
    onStart.invoke()
    api().enqueue(object : Callback<ResponseBean<T>> {
        override fun onResponse(call: Call<ResponseBean<T>>, response: Response<ResponseBean<T>>) {
            val responseBean = response.body()
            if (responseBean != null && responseBean.isSuccess() && responseBean.response != null) {
                onSuccess.invoke(responseBean.response!!)
            } else {
                onFailed.invoke("other error")
            }
        }

        override fun onFailure(call: Call<ResponseBean<T>>, t: Throwable) {
            onFailed.invoke("Network error")
        }
    })
}