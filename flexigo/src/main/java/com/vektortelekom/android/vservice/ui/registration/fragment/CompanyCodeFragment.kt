package com.vektortelekom.android.vservice.ui.registration.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.RegisterVerifyCompanyCodeRequest
import com.vektortelekom.android.vservice.databinding.CompanyCodeFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.registration.RegistrationViewModel
import javax.inject.Inject

class CompanyCodeFragment : BaseFragment<RegistrationViewModel>(), TextWatcher {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    lateinit var binding: CompanyCodeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CompanyCodeFragmentBinding>(inflater, R.layout.company_code_fragment, container, false).apply {
            lifecycleOwner = this@CompanyCodeFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logo = requireActivity().findViewById<View>(R.id.imageview_logo) as AppCompatImageView
        logo.visibility = View.GONE


        binding.edittextCode.setText(viewModel.companyAuthCode.value)

        viewModel.companyAuthCode.observe(viewLifecycleOwner){
            if (it != null && !it.equals("")) {
                binding.buttonContinue.isEnabled = true
                binding.edittextCode.setText(it)
            }
        }

        viewModel.isCompanyCodeSuccess.observe(viewLifecycleOwner){
            if (it != null && it) {
                NavHostFragment.findNavController(this).navigate(R.id.action_companyCodeFragment_to_emailCodeFragment)
                viewModel.isCompanyCodeSuccess.value = false
            }
        }

        binding.buttonContinue.setOnClickListener{
            val request = RegisterVerifyCompanyCodeRequest(viewModel.userName, viewModel.userSurname, viewModel.userEmail, viewModel.userPassword, viewModel.companyAuthCode.value)
            viewModel.sendCompanyCode(request, resources.configuration.locale.language)
        }

        binding.imageviewQrScan.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_companyCodeFragment_to_companyCodeQrReaderFragment)
        }

        binding.imageviewBack.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

    }

    override fun onResume() {
        super.onResume()

        binding.edittextCode.addTextChangedListener(this)
    }

    override fun getViewModel(): RegistrationViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RegistrationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CompanyCodeFragment"
        fun newInstance() = CompanyCodeFragment()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {

        if (s != null) {
            if (s.length > 2 && viewModel.companyAuthCode.value != s.toString()) {
                viewModel.companyAuthCode.value = s.toString()
            }
        }
    }


}