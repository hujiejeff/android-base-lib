package com.hujiejeff.library.base.bus

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap


object FlowBus {

    private var events = ConcurrentHashMap<Any, MutableSharedFlow<Any>>()
    private var stickyEvents = ConcurrentHashMap<Any, MutableSharedFlow<Any>>()

    @JvmStatic
    inline fun <reified T> with() = with(T::class.java)

    @JvmStatic
    inline fun <reified T> withSticky() = withSticky(T::class.java)


    @JvmStatic
    fun <T> with(key: Class<T>): WrapBus<T> {
        if (!events.containsKey(key)) {
            events[key] = MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)
        }
        return WrapBus(events[key] as MutableSharedFlow<T>)
    }

    @JvmStatic
    fun <T> withSticky(key: Class<T>): WrapBus<T> {
        if (!stickyEvents.containsKey(key)) {
            stickyEvents[key] = MutableSharedFlow(1, 1, BufferOverflow.DROP_OLDEST)
        }
        return WrapBus(stickyEvents[key] as MutableSharedFlow<T>)
    }


    @JvmStatic
    fun <T> on(key: Class<T>): LiveData<T> {
        return with(key).flow.asLiveData()
    }

    @JvmStatic
    fun <T> onSticky(key: Class<T>): LiveData<T> {
        return withSticky(key).flow.asLiveData()
    }

    class WrapBus<T>(val flow: MutableSharedFlow<T>) {
        fun post(t: T) {
            flow.tryEmit(t)
        }
    }
}