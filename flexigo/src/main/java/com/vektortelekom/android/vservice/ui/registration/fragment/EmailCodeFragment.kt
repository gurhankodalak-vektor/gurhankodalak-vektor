package com.vektortelekom.android.vservice.ui.registration.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.CheckDomainRequest
import com.vektortelekom.android.vservice.data.model.EmailVerifyEmailRequest
import com.vektortelekom.android.vservice.databinding.EmailCodeFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.registration.RegistrationViewModel
import javax.inject.Inject

class EmailCodeFragment : BaseFragment<RegistrationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    lateinit var binding: EmailCodeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<EmailCodeFragmentBinding>(inflater, R.layout.email_code_fragment, container, false).apply {
            lifecycleOwner = this@EmailCodeFragment
            viewModel = this@EmailCodeFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textviewSendAgain.setOnClickListener{

            val request = CheckDomainRequest(viewModel.userName.value, viewModel.userSurname.value, viewModel.userEmail.value, viewModel.userPassword.value)
            viewModel.checkDomain(request, resources.configuration.locale.language)

        }

        binding.textviewMailAgain.setOnClickListener{
            NavHostFragment.findNavController(this).navigateUp()
        }

        binding.buttonSubmit.setOnClickListener{
            // TODO: ASD kodu geçici olarak ekledim. 
            val request = EmailVerifyEmailRequest(viewModel.userName.value, viewModel.userSurname.value, viewModel.userEmail.value, viewModel.userPassword.value, "ASD", binding.edittextCode.text.toString())
            viewModel.verifyEmail(request, resources.configuration.locale.language)
        }
        
        viewModel.isVerifySuccess.observe(viewLifecycleOwner){
            viewModel.getDestinations()
        }
        viewModel.getDestinations()
        viewModel.destinations.observe(viewLifecycleOwner){
            if (it != null && it.size > 1)
                NavHostFragment.findNavController(this).navigate(R.id.action_emailCodeFragment_to_selectCampusFragment)

        }


    }

    override fun getViewModel(): RegistrationViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RegistrationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "EmailCodeFragment"
        fun newInstance() = EmailCodeFragment()
    }


}