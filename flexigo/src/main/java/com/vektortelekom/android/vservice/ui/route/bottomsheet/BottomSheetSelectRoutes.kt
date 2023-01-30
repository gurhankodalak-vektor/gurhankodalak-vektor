package com.vektortelekom.android.vservice.ui.route.bottomsheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.BottomSheetSelectRoutesBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.route.RouteSelectionActivity
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import javax.inject.Inject

class BottomSheetSelectRoutes : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: BottomSheetSelectRoutesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetSelectRoutesBinding>(inflater, R.layout.bottom_sheet_select_routes, container, false).apply {
            lifecycleOwner = this@BottomSheetSelectRoutes
            viewModel = this@BottomSheetSelectRoutes.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutSearch1.setOnClickListener{
            val intent = Intent(requireContext(), RouteSelectionActivity::class.java)
            startActivity(intent)
        }
        binding.layoutSearch2.setOnClickListener{
            viewModel.noRoutesForEdit.value = true
        }
        binding.layoutSearch3.setOnClickListener{
            viewModel.bottomSheetBehaviorEditShuttleState.value = BottomSheetBehavior.STATE_HIDDEN
            viewModel.navigator?.showInformationFragment()
        }


    }


    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "BottomSheetSelectRoutes"
        fun newInstance() = BottomSheetSelectRoutes()

    }

}