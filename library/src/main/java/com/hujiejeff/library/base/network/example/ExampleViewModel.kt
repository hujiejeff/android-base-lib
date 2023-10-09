package com.hujiejeff.library.base.network.example

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hujiejeff.library.base.network.entity.isSuccess
import com.hujiejeff.library.base.network.util.RequestState
import com.hujiejeff.library.base.network.util.asRequestFlow
import com.hujiejeff.library.base.network.util.asUiStateFlow
import com.hujiejeff.library.base.network.util.originalRequest
import com.hujiejeff.library.base.network.util.singleRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update

class ExampleViewModel : ViewModel() {
    /**
     * 通过LiveData请求数据
     * -------------------------------------------------------
     */
    private val doRequestLiveData = MutableLiveData<Boolean>()
    val sampleLiveData = Transformations.switchMap(doRequestLiveData) {
        ExampleHttp.getApi<ExampleApi>().getBannersByLiveData()
    }

    fun doLiveDataRequest() {
        doRequestLiveData.value = true
    }

    /**
     * 通过Flow 请求数据
     * ------------------------------------------------------
     */
    private val doRequestFlow = MutableStateFlow<Int>(0)

    private val flowAPI: Flow<UiState> =
        doRequestFlow.transform {
            Log.d("hujie", ": transform")
            emit(UiState.DataLoading)
            delay(2000L)
            val originData: Flow<UiState> = ExampleHttp.getApi<ExampleApi>().getBannersByFlow()
                .map {
                    if (it.isSuccess()) {
                        UiState.Success(it.response!!.data!!)
                    } else {
                        UiState.Failed(it.response!!.errorMsg)
                    }
                }
            emitAll(originData)
        }

    val sampleFlowData: StateFlow<UiState> =
        flowAPI.stateIn(
            scope = viewModelScope,
            initialValue = UiState.FirstLoading,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun doFlowRequest() {
        doRequestFlow.update { it + 1 }
    }


    /**
     * 通过工具类简化flow请求
     * ---------------------------------------------------
     */

    private val doRequestFlow2 = MutableStateFlow(0)

    private val apiFlow = doRequestFlow2.asRequestFlow {
        ExampleHttp.getApi<ExampleApi>().getBannersByFlow()
    }.map { reqSate ->
        when (reqSate) {
            is RequestState.Success -> {
                UiState.Success(reqSate.data.data!!)
            }

            is RequestState.Loading -> {
                UiState.DataLoading
            }

            is RequestState.Failed -> {
                UiState.Failed(reqSate.msg)
            }
        }

    }

    val sampleStateFlow2: StateFlow<UiState> = asUiStateFlow(apiFlow, UiState.FirstLoading)

    fun doRequestFlow2() {
        doRequestFlow2.update {it + 1}
    }

    /**
     * 通过协程请求数据
     * ----------------------------------------------------------
     */
    private val _sampleFlowDataForCoroutine = MutableStateFlow<UiState>(UiState.FirstLoading)
    val sampleFlowDataForCoroutine = _sampleFlowDataForCoroutine.asStateFlow()
    fun doQuestCoroutines() {
        singleRequest(
            api = { ExampleHttp.getApi<ExampleApi>().getBanners() },
            onStart = {
                _sampleFlowDataForCoroutine.update {
                    UiState.DataLoading
                }
            },
            onSuccess = { data ->
                _sampleFlowDataForCoroutine.update {
                    UiState.Success(data.data!!)
                }
            },
            onFailed = { error ->
                _sampleFlowDataForCoroutine.update {
                    UiState.Failed(error)
                }
            })
    }

    /**
     * 简单原始请求for java
     */

    fun doCallRequest() {
        originalRequest(
            api = { ExampleHttp.getApi<ExampleApi>().getBannerByCall() },
            onStart = {
                _sampleFlowDataForCoroutine.update {
                    UiState.DataLoading
                }
            },
            onSuccess = { data ->
                _sampleFlowDataForCoroutine.update {
                    UiState.Success(data.data!!)
                }
            },
            onFailed = { error ->
                _sampleFlowDataForCoroutine.update {
                    UiState.Failed(error)
                }
            })
    }

    sealed class UiState {
        object FirstLoading : UiState()
        data class Success(val data: List<BannerBean>) : UiState()
        object DataLoading : UiState()
        data class Failed(val msg: String) : UiState()
    }



}