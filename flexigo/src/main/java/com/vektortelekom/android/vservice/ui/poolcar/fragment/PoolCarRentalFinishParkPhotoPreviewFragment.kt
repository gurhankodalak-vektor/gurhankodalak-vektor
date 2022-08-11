package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarRentalFinishParkPhotoPreviewFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import javax.inject.Inject

class PoolCarRentalFinishParkPhotoPreviewFragment: BaseFragment<PoolCarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    lateinit var binding: PoolCarRentalFinishParkPhotoPreviewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarRentalFinishParkPhotoPreviewFragmentBinding>(inflater, R.layout.pool_car_rental_finish_park_photo_preview_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarRentalFinishParkPhotoPreviewFragment
            viewModel = this@PoolCarRentalFinishParkPhotoPreviewFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlideApp.with(requireActivity()).load(viewModel.finishParkInfoPreviewPhotoPath).into(binding.imageViewPreview)

    }

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarRentalFinishParkPhotoPreviewFragment"

        fun newInstance() = PoolCarRentalFinishParkPhotoPreviewFragment()

    }

}