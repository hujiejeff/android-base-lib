package com.hujiejeff.library.base.base

import androidx.lifecycle.ViewModel

open class BaseViewModel: ViewModel() {
    sealed class UiState<T> {
        class Success<T>(val data: T, val page: Int) : UiState<T>()
        class DataLoading<T> : UiState<T>()
        class Failed<T>(val msg: String) : UiState<T>()
        class Init<T>() : UiState<T>()
    }
}