package com.hujiejeff.library.base.network.entity

/**
 * 转换后的网络请求结构，包含原始响应Json，以及添加网络错误附加信息
 */
data class ResponseBean<T>(
    val response: T?,
    val errorCode: ErrorType = ErrorType.SUCCESS,
    val errorMsg: String = ""
)

fun <T> ResponseBean<T>.isSuccess() = errorCode == ErrorType.SUCCESS


enum class ErrorType {
    SUCCESS, UN_HOST, NOT_FOUND, SERVER_ERROR,OTHER_EXCEPTION
}