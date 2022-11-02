package com.vektortelekom.android.vservice.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.vektor.ktx.data.local.StateManager
import com.vektortelekom.android.vservice.utils.AnalyticsManager
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

abstract class BaseDialogFragment<T : ViewModel> : DaggerDialogFragment() {

    @Inject
    lateinit var stateManager: StateManager

    private var viewModel: T? = null

    abstract fun getViewModel(): T

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = getViewModel()
    }


    override fun onResume() {
        super.onResume()

        context?.let {
            AnalyticsManager.build(it).sendFragmentScreenEvent(this)
        }
    }

}