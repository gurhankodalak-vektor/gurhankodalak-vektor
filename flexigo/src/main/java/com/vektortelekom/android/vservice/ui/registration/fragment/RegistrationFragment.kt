package com.vektortelekom.android.vservice.ui.registration.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.CheckDomainRequest
import com.vektortelekom.android.vservice.databinding.RegistrationFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.registration.RegistrationViewModel
import com.vektortelekom.android.vservice.utils.PasswordStrength
import com.vektortelekom.android.vservice.utils.isValidEmail
import javax.inject.Inject


class RegistrationFragment : BaseFragment<RegistrationViewModel>(), TextWatcher {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    lateinit var binding: RegistrationFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<RegistrationFragmentBinding>(inflater, R.layout.registration_fragment, container, false).apply {
            lifecycleOwner = this@RegistrationFragment
            viewModel = this@RegistrationFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTextErrors()

        binding.editTextPassword.addTextChangedListener(this)

        binding.textviewSignText.setOnClickListener{
            activity?.finish()
        }

        binding.layoutWarning.visibility = View.GONE

        binding.buttonSignup.setOnClickListener{
            viewModel.userName.value = binding.edittextName.text.toString()
            viewModel.userSurname.value = binding.edittextSurname.text.toString()
            viewModel.userEmail.value = binding.edittextMail.text.toString()
            viewModel.userPassword.value = binding.editTextPassword.text.toString()

            val request = CheckDomainRequest(binding.edittextName.text.toString(), binding.edittextSurname.text.toString(), binding.edittextMail.text.toString(), binding.editTextPassword.text.toString())
            viewModel.checkDomain(request, resources.configuration.locale.language)


//            NavHostFragment.findNavController(this).navigate(R.id.action_registrationFragment_to_emailCodeFragment)
        }

        viewModel.isCompanyAuthCodeRequired.observe(viewLifecycleOwner) {
            if (it != null && !it)
                NavHostFragment.findNavController(this).navigate(R.id.action_registrationFragment_to_emailCodeFragment)
        }

    }

    private fun setTextErrors() {

        binding.edittextMail.addTextChangedListener {
            if(it.toString().isValidEmail()) {
                binding.textInputLayoutEmail.error = null
            }
            else {
                if (it.toString().isNotEmpty())
                    binding.textInputLayoutEmail.error = getString(R.string.check_information)
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        updatePasswordStrengthView(p0.toString())
    }

    override fun afterTextChanged(p0: Editable?) {}

    private fun updatePasswordStrengthView(password: String) {

        if (password.isEmpty())
            binding.layoutWarning.visibility = View.GONE
        else
            binding.layoutWarning.visibility = View.VISIBLE


        val strength = PasswordStrength.calculateStrength(password)

        when (strength.name) {
            "WEAK" -> {
                binding.line1.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPinkishRed))
                binding.line2.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
                binding.line3.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
                binding.line4.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
            }
            "MEDIUM" -> {
                binding.line1.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orangeYellow))
                binding.line2.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orangeYellow))
                binding.line3.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
                binding.line4.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
            }
            "STRONG" -> {
                binding.line1.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.sunflowerYellow))
                binding.line2.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.sunflowerYellow))
                binding.line3.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.sunflowerYellow))
                binding.line4.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
            }
            "VERY_STRONG" -> {
                binding.line1.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAquaGreen))
                binding.line2.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAquaGreen))
                binding.line3.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAquaGreen))
                binding.line4.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAquaGreen))
            }
            "NONE" -> {
                binding.line1.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
                binding.line2.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
                binding.line3.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
                binding.line4.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorIceBlue))
            }
        }

        if (password.length < 6)
            binding.imageview1.setImageResource(R.drawable.ic_check_icon_grey)
        else
            binding.imageview1.setImageResource(R.drawable.ic_check_icon_green)


       getCharacterStatus(password)

        if (!sawUpperLetter)
            binding.imageview2.setImageResource(R.drawable.ic_check_icon_grey)
        else
            binding.imageview2.setImageResource(R.drawable.ic_check_icon_green)

        if (!sawLowerLetter)
            binding.imageview3.setImageResource(R.drawable.ic_check_icon_grey)
        else
            binding.imageview3.setImageResource(R.drawable.ic_check_icon_green)

        if (!sawDigitLetter)
            binding.imageview4.setImageResource(R.drawable.ic_check_icon_grey)
        else
            binding.imageview4.setImageResource(R.drawable.ic_check_icon_green)

        if (password.length >= 6 && sawDigitLetter && sawLowerLetter && sawUpperLetter)
            binding.layoutWarning.visibility = View.GONE
        else
            binding.layoutWarning.visibility = View.VISIBLE


    }

    private var sawUpperLetter = false
    private var sawLowerLetter = false
    private var sawDigitLetter = false

    private fun getCharacterStatus(password: String){
        var sawUpper = false
        var sawLower = false
        var sawDigit = false

        for (element in password) {

                if (!sawDigit && Character.isDigit(element)) {
                    sawDigit = true
                } else {
                        if (Character.isUpperCase(element))
                            sawUpper = true
                        else if(Character.isLowerCase(element))
                            sawLower = true
                }

        }

        sawUpperLetter = sawUpper
        sawLowerLetter = sawLower
        sawDigitLetter = sawDigit

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