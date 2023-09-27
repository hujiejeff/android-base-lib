package com.szpgm.commonlib.network.entity

data class ResponseBean<T>(
    val data: T?,
    val errorCode: ErrorType = ErrorType.SUCCESS,
    val errorMsg: String = ""
)

fun <T> ResponseBean<T>.isSuccess() = errorCode == ErrorType.SUCCESS


enum class ErrorType {
    SUCCESS, UN_HOST, NOT_FOUND, SERVER_ERROR,
}