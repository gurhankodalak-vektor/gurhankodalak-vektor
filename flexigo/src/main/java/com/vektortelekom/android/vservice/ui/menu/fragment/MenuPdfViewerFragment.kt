package com.vektortelekom.android.vservice.ui.menu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.MenuPdfViewerFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.menu.MenuViewModel
import javax.inject.Inject

class MenuPdfViewerFragment : BaseFragment<MenuViewModel>()  {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    private lateinit var binding: MenuPdfViewerFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<MenuPdfViewerFragmentBinding>(inflater, R.layout.menu_pdf_viewer_fragment, container, false).apply {
            lifecycleOwner = this@MenuPdfViewerFragment
            viewModel = this@MenuPdfViewerFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            viewModel.pdfUrl = bundle.getString("url")
        }

        viewModel.getPrivacyPolicy()

        viewModel.privacyPolicy.observe(viewLifecycleOwner) { response ->

            binding.pdfViewer.fromStream(response.byteStream()).load()

        }


    }

    override fun getViewModel(): MenuViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[MenuViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "MenuPdfViewerFragment"
        fun newInstance() = MenuPdfViewerFragment()
    }

}