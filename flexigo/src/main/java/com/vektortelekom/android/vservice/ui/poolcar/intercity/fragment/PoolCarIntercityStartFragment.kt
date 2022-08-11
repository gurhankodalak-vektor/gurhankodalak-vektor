package com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarIntercityStartFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.poolcar.intercity.PoolCarIntercityViewModel
import javax.inject.Inject

class PoolCarIntercityStartFragment: BaseFragment<PoolCarIntercityViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarIntercityViewModel

    lateinit var binding: PoolCarIntercityStartFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarIntercityStartFragmentBinding>(inflater, R.layout.pool_car_intercity_start_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarIntercityStartFragment
            viewModel = this@PoolCarIntercityStartFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.createRentalResponse.observe(viewLifecycleOwner) {
            FlexigoInfoDialog.Builder(requireContext())
                    .setText1(getString(R.string.pool_car_intercity_create_success))
                    .setCancelable(false)
                    .setIconVisibility(true)
                    .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                        dialog.dismiss()
                        viewModel.vehicleMake.value = viewModel.vehicleMake.value?.plus(" ").plus(viewModel.vehicleModel.value)
                        viewModel.navigator?.showRentalFragment()
                    }
                    .create()
                    .show()
        }

    }

    override fun getViewModel(): PoolCarIntercityViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarIntercityViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarIntercityStartFragment"

        fun newInstance() = PoolCarIntercityStartFragment()

    }

}