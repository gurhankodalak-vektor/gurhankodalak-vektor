package com.vektortelekom.android.vservice.ui.menu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.MenuDrivingLicencePreviewFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.menu.MenuViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import com.vektortelekom.android.vservice.utils.convertBackendDateToMMMddyyyyFormat
import javax.inject.Inject

class MenuDrivingLicencePreviewFragment : BaseFragment<MenuViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    lateinit var binding: MenuDrivingLicencePreviewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<MenuDrivingLicencePreviewFragmentBinding>(inflater, R.layout.menu_driving_licence_preview_fragment, container, false).apply {
            lifecycleOwner = this@MenuDrivingLicencePreviewFragment
            viewModel = this@MenuDrivingLicencePreviewFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.latestDrivingLicenceDocument.value.let {

            binding.textViewDrivingLicenseNumber.text = it?.documentNumber
            binding.textViewDrivingLicenseExpireDate.text = it?.validUntil.convertBackendDateToMMMddyyyyFormat()
            binding.textViewDrivingLicenseGivenDate.text = it?.issueDate.convertBackendDateToMMMddyyyyFormat()

            when (it?.documentStatus) {
                "PENDING_APPROVAL" -> {
                    binding.textViewDocumentStatus.text = binding.root.context?.getString(R.string.expectant)
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.marigold))
                }
                "APPROVED" -> {
                    binding.textViewDocumentStatus.text =  binding.root.context?.getString(R.string.accept)
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAquaGreen))
                }
                "REJECTED" -> {
                    binding.textViewDocumentStatus.text = binding.root.context?.getString(R.string.rejected)
                    binding.cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.watermelon))
                }
            }

            val frontUrl: String = AppApiHelper().baseUrl2
                .plus("/")
                .plus("report/fileViewer/uuid/")
                .plus(it?.photoUuid1)

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.placeholder_black)
                .error(R.drawable.placeholder_black)

            GlideApp.with(requireActivity()).setDefaultRequestOptions(requestOptions).load(frontUrl).into(binding.imageViewLicenceFront)

            val rearUrl: String = AppApiHelper().baseUrl2
                .plus("/")
                .plus("report/fileViewer/uuid/")
                .plus(it?.photoUuid2)


            GlideApp.with(requireActivity()).setDefaultRequestOptions(requestOptions).load(rearUrl).into(binding.imageViewLicenceRear)
        }

        binding.buttonUpdate.setOnClickListener {
            viewModel.navigator?.showMenuDrivingLicenseDialog(null)
        }
    }

    override fun getViewModel(): MenuViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[MenuViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "MenuDrivingLicencePreviewFragment"

        fun newInstance() = MenuDrivingLicencePreviewFragment()

    }
}