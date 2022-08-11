package com.vektortelekom.android.vservice.ui.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.LoginForgotPasswordFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.login.LoginViewModel
import com.vektortelekom.android.vservice.utils.isValidEmail
import javax.inject.Inject

class ForgotPasswordFragment : BaseFragment<LoginViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: LoginViewModel

    lateinit var binding: LoginForgotPasswordFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate<LoginForgotPasswordFragmentBinding>(inflater, R.layout.login_forgot_password_fragment, container, false).apply {
            lifecycleOwner = this@ForgotPasswordFragment
            viewModel = this@ForgotPasswordFragment.viewModel
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.forgotPasswordResponse.observe(viewLifecycleOwner) {

            if (it != null) {
                val dialog = AppDialog.Builder(requireContext())
                        .setIconVisibility(true)
                        .setTitle(R.string.change_password_success)
                        .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                            activity?.onBackPressed()
                        }
                        .create()

                dialog.show()

                viewModel.forgotPasswordResponse.value = null
            }

        }

        setTextErrors()
    }

    private fun setTextErrors() {
        binding.editTextEmail.addTextChangedListener {

            if(it.toString().isValidEmail()) {
                binding.textInputLayoutEmail.error = null
            }
            else {
                binding.textInputLayoutEmail.error = getString(R.string.e_mail_error)
            }

        }
    }

    override fun getViewModel(): LoginViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[LoginViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "ForgotPasswordFragment"
        fun newInstance() = ForgotPasswordFragment()
    }

}