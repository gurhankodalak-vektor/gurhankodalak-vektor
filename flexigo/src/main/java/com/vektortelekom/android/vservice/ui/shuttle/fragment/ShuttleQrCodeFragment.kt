package com.vektortelekom.android.vservice.ui.shuttle.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.ShuttleQrFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import javax.inject.Inject

class ShuttleQrCodeFragment : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: ShuttleQrFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ShuttleQrFragmentBinding>(inflater, R.layout.shuttle_qr_fragment, container, false).apply {
            lifecycleOwner = this@ShuttleQrCodeFragment
            viewModel = this@ShuttleQrCodeFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchQr.isChecked = AppDataManager.instance.isQrAutoOpen

    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "ShuttleQrCodeFragment"

        fun newInstance() = ShuttleQrCodeFragment()

    }

}