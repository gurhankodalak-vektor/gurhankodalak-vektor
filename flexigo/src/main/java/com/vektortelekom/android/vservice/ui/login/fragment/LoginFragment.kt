package com.vektortelekom.android.vservice.ui.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.LoginFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.login.LoginViewModel
import com.vektortelekom.android.vservice.utils.isValidEmail
import javax.inject.Inject

class LoginFragment : BaseFragment<LoginViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: LoginViewModel

    lateinit var binding: LoginFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<LoginFragmentBinding>(inflater, R.layout.login_fragment, container, false).apply {
            lifecycleOwner = this@LoginFragment
            viewModel = this@LoginFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppDataManager.instance.rememberMe?.let {
            viewModel.isRememberMe.value = it
        }

        AppDataManager.instance.userName?.let {
            viewModel.loginEmail.value = it
        }

        AppDataManager.instance.password?.let {
            viewModel.loginPassword.value = it
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

        binding.editTextPassword.addTextChangedListener {

            if((it?.length ?: 0) > 3) {
                binding.textInputLayoutPassword.error = null
            }
            else {
                binding.textInputLayoutPassword.error = getString(R.string.password_error)
            }

        }

    }

    override fun getViewModel(): LoginViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[LoginViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "LoginFragment"
        fun newInstance() = LoginFragment()
    }

}