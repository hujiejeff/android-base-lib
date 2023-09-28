package com.hujiejeff.library.base.network.example

/**
 * 你的原始网络请求JSON结构
 */
data class ExampleRespBean<T>(val data: T?, val errorCode: Int = 0, val errorMsg: String = "")


data class BannerBean(
    val desc: String,
    val id: Int,
    val imagePath: String,
    val isVisible: Int,
    val order: Int,
    val title: String,
    val type: Int,
    val url: String
)