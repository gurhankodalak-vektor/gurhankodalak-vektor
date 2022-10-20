package com.vektortelekom.android.vservice.ui.registration.fragment

import android.content.Intent
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
import com.vektortelekom.android.vservice.ui.login.LoginActivity
import com.vektortelekom.android.vservice.ui.registration.RegistrationViewModel
import com.vektortelekom.android.vservice.ui.survey.SurveyActivity
import javax.inject.Inject

class EmailCodeFragment : BaseFragment<RegistrationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    lateinit var binding: EmailCodeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<EmailCodeFragmentBinding>(inflater, R.layout.email_code_fragment, container, false).apply {
            lifecycleOwner = this@EmailCodeFragment
            viewModel = this@EmailCodeFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textviewSendAgain.setOnClickListener{
            val request = CheckDomainRequest(viewModel.userName, viewModel.userSurname, viewModel.userEmail, viewModel.userPassword)
            viewModel.checkDomain(request, resources.configuration.locale.language)
        }

        binding.buttonMailAgain.setOnClickListener{
            NavHostFragment.findNavController(this).navigateUp()
        }

        binding.buttonSubmit.setOnClickListener{
            // TODO: ASD kodu geçici olarak ekledim. 
            val request = EmailVerifyEmailRequest(viewModel.userName, viewModel.userSurname, viewModel.userEmail, viewModel.userPassword, "ASD", binding.edittextCode.text.toString())
            viewModel.verifyEmail(request, resources.configuration.locale.language)
        }
        
        viewModel.isVerifySuccess.observe(viewLifecycleOwner){
            stateManager.vektorToken = viewModel.sessionId.value
        }

        viewModel.verifyEmailResponse.observe(viewLifecycleOwner){
            if (it.personnel.destination == null || (it != null && it.personnel.destination.id == 0L))
                NavHostFragment.findNavController(this).navigate(R.id.action_emailCodeFragment_to_selectCampusFragment)
            else{
                if (viewModel.surveyQuestionId.value != null){
                    activity?.finish()
                    val intent = Intent(requireActivity(), SurveyActivity::class.java)
                    intent.putExtra("surveyQuestionId", viewModel.surveyQuestionId.value)
                    startActivity(intent)
                } else{
                    activity?.finish()
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    startActivity(intent)
                }

            }

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