package com.vektortelekom.android.vservice.ui.registration

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.RegistrationFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.MessageBoxDialog
import com.vektortelekom.android.vservice.utils.AppConstants
import com.vektortelekom.android.vservice.utils.isValidEmail
import javax.inject.Inject

class RegistrationFragment : BaseFragment<RegistrationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    lateinit var binding: RegistrationFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<RegistrationFragmentBinding>(inflater, R.layout.registration_fragment, container, false).apply {
            lifecycleOwner = this@RegistrationFragment
            viewModel = this@RegistrationFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun showPasswordDialog() {
        MessageBoxDialog.Builder(requireContext())
            .setCloseButtonVisibility(false)
            .setIconVisibility(true)
            .setIcon(R.drawable.ic_warning)
            .setTitle(getString(R.string.Generic_Err))
            .setOkButton(getString(R.string.call_call_center)) {
            }.create().show()
    }

    override fun getViewModel(): RegistrationViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RegistrationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "RegistrationFragment"
        fun newInstance() = RegistrationFragment()
    }

}