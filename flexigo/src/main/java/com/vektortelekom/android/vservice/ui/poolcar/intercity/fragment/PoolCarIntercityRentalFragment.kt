package com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarIntercityRentalFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.intercity.PoolCarIntercityViewModel
import javax.inject.Inject

class PoolCarIntercityRentalFragment: BaseFragment<PoolCarIntercityViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarIntercityViewModel

    lateinit var binding: PoolCarIntercityRentalFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarIntercityRentalFragmentBinding>(inflater, R.layout.pool_car_intercity_rental_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarIntercityRentalFragment
            viewModel = this@PoolCarIntercityRentalFragment.viewModel
        }

        return binding.root
    }

    override fun getViewModel(): PoolCarIntercityViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarIntercityViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarIntercityRentalFragment"

        fun newInstance() = PoolCarIntercityRentalFragment()

    }

}