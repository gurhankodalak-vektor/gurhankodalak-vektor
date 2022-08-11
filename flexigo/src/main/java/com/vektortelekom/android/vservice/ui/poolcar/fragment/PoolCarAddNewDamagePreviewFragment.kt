package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarAddNewDamagePreviewFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import javax.inject.Inject

class PoolCarAddNewDamagePreviewFragment: BaseFragment<PoolCarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    lateinit var binding: PoolCarAddNewDamagePreviewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarAddNewDamagePreviewFragmentBinding>(inflater, R.layout.pool_car_add_new_damage_preview_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarAddNewDamagePreviewFragment
            viewModel = this@PoolCarAddNewDamagePreviewFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(viewModel.isPreviewExternal == true) {
            GlideApp.with(requireActivity()).load(viewModel.addNewDamagePreviewPhotoPath).into(binding.imageViewPreview)
        }
        else {
            GlideApp.with(requireActivity()).load(viewModel.addNewInternalDamagePreviewPhotoPath).into(binding.imageViewPreview)
        }

    }

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarAddNewDamagePreviewFragment"

        fun newInstance() = PoolCarAddNewDamagePreviewFragment()

    }

}