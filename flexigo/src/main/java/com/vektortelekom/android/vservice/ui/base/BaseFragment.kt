package com.vektortelekom.android.vservice.ui.base

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.vektor.ktx.data.local.StateManager
import com.vektortelekom.android.vservice.utils.AnalyticsManager
import dagger.android.support.DaggerFragment
import javax.inject.Inject

abstract class BaseFragment<T : ViewModel> : DaggerFragment() {

    @Inject
    lateinit var stateManager: StateManager

    private var viewModel: T? = null

    abstract fun getViewModel(): T

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = getViewModel()
    }

    fun showToast(message: String) {
        val toast = Toast.makeText(this.context, message, Toast.LENGTH_SHORT)
        toast.show()
    }


    override fun onResume() {
        super.onResume()

        context?.let {
            AnalyticsManager.build(it).sendFragmentScreenEvent(this)
        }
    }

}