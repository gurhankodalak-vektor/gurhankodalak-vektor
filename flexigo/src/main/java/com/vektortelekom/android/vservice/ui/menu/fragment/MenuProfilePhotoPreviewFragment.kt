package com.vektortelekom.android.vservice.ui.menu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.MenuProfilePhotoPreviewFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.menu.MenuViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import javax.inject.Inject

class MenuProfilePhotoPreviewFragment: BaseFragment<MenuViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    private lateinit var binding: MenuProfilePhotoPreviewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<MenuProfilePhotoPreviewFragmentBinding>(inflater, R.layout.menu_profile_photo_preview_fragment, container, false).apply {
            lifecycleOwner = this@MenuProfilePhotoPreviewFragment
            viewModel = this@MenuProfilePhotoPreviewFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlideApp.with(requireContext()).load(viewModel.previewPhotoPath).into(binding.imageViewPreview)

    }

    override fun getViewModel(): MenuViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[MenuViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "MenuProfilePhotoPreviewFragment"

        fun newInstance() = MenuProfilePhotoPreviewFragment()

    }

}