package com.vektortelekom.android.vservice.ui.comments.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CommentsPhotoPreviewFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.comments.CommentsViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import javax.inject.Inject

class CommentsPhotoPreviewFragment  : BaseFragment<CommentsViewModel>()  {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CommentsViewModel

    private lateinit var binding: CommentsPhotoPreviewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CommentsPhotoPreviewFragmentBinding>(inflater, R.layout.comments_photo_preview_fragment, container, false).apply {
            lifecycleOwner = this@CommentsPhotoPreviewFragment
            viewModel = this@CommentsPhotoPreviewFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlideApp.with(requireContext()).load(viewModel.previewPhotoPath).into(binding.imageViewPreview)

    }

    override fun getViewModel(): CommentsViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CommentsViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CommentsPhotoPreviewFragment"

        fun newInstance() = CommentsPhotoPreviewFragment()

    }

}