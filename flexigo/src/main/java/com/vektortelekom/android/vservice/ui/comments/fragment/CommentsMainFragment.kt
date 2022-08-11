package com.vektortelekom.android.vservice.ui.comments.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CommentsMainFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.base.HighlightView
import com.vektortelekom.android.vservice.ui.comments.CommentsViewModel
import com.vektortelekom.android.vservice.ui.comments.adapters.CommentListAdapter
import javax.inject.Inject

class CommentsMainFragment : BaseFragment<CommentsViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CommentsViewModel

    lateinit var binding: CommentsMainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CommentsMainFragmentBinding>(inflater, R.layout.comments_main_fragment, container, false).apply {
            lifecycleOwner = this@CommentsMainFragment
            viewModel = this@CommentsMainFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tickets.observe(viewLifecycleOwner) { response ->
            binding.recyclerViewComments.adapter = CommentListAdapter(response)
        }

        HighlightView.Builder(requireContext(), binding.cardViewAddComment, requireActivity(), "button_add_comment", "sequence_comments_main_fragment")
                .setHighlightText(getString(R.string.tutorial_comments_add))
                .addGotItListener {

                }
                .create()

    }

    override fun getViewModel(): CommentsViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CommentsViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CommentsMainFragment"

        fun newInstance() = CommentsMainFragment()

    }

}