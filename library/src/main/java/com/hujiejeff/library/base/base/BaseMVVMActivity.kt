package com.hujiejeff.library.base.base

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.hujiejeff.library.base.network.util.collectStateFlow
import com.hujiejeff.library.base.util.setActivityContentView
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

abstract class BaseMVVMActivity<V: ViewBinding, VM: ViewModel>: AppCompatActivity() {
    protected lateinit var mBinding: V
    protected lateinit var mViewModel: VM
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = provideViewModel()
        mBinding = setActivityContentView(layoutInflater)!!
        initUI()
        initEvent()
        initData()
        initCollect()
    }

    abstract fun provideViewModel(): VM

    protected open fun initUI() {

    }

    protected open fun initEvent() {

    }

    protected open fun initData() {

    }

    protected open fun initCollect() {

    }


    fun <T> collect(stateFlow: StateFlow<T>, collector: FlowCollector<T>) {
        collectStateFlow(Lifecycle.State.STARTED) {
            stateFlow.collect(collector)
        }
    }

    protected inline fun <reified VM: ViewModel> getViewModel(): VM {
        val viewModel by viewModels<VM>()
        return viewModel
    }

    protected fun<VM: ViewModel> getViewModel(clazz: Class<VM>): VM {
        return ViewModelProvider(
            viewModelStore,
            defaultViewModelProviderFactory
        )[clazz]
    }
}