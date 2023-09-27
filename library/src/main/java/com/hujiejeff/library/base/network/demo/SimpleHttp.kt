package com.hujiejeff.library.base.network.demo

import android.util.Log
import com.hujiejeff.library.base.network.HttpAbstract
import com.hujiejeff.library.base.network.demo.SimpleApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


object SimpleHttp: HttpAbstract() {
    override val baseUrl: String = "https://www.wanandroid.com"
    override val useStandardResponseBean: Boolean = true

    /**
     * 协程方式调用
     */
    fun test() {
        val simpleApi: SimpleApi = getApi()
        GlobalScope.launch {
            Log.d("Test","" + simpleApi.getBanners())
        }
    }
}