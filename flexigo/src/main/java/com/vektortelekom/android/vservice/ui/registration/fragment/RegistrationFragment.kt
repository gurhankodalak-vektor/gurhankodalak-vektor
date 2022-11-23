package com.vektortelekom.android.vservice.ui.registration.fragment

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.skydoves.balloon.balloon
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.CheckDomainRequest
import com.vektortelekom.android.vservice.databinding.RegistrationFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.registration.RegistrationViewModel
import com.vektortelekom.android.vservice.utils.PasswordStrength
import com.vektortelekom.android.vservice.utils.isValidEmail
import com.vektortelekom.android.vservice.utils.tooltip.TooltipBalloon
import javax.inject.Inject
import kotlin.math.abs


class RegistrationFragment : BaseFragment<RegistrationViewModel>(), TextWatcher {

    private val mTooltipBalloon by balloon(TooltipBalloon::class)

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    lateinit var binding: RegistrationFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<RegistrationFragmentBinding>(inflater, R.layout.registration_fragment, container, false).apply {
            lifecycleOwner = this@RegistrationFragment
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

        binding.buttonSignup.setOnClickListener{

            viewModel.userName = binding.edittextName.text.toString()
            viewModel.userSurname = binding.edittextSurname.text.toString()
            viewModel.userEmail = binding.edittextMail.text.toString().trim()
            viewModel.userPassword = binding.editTextPassword.text.toString()

            val request = CheckDomainRequest(binding.edittextName.text.toString(), binding.edittextSurname.text.toString(), binding.edittextMail.text.toString().trim(), binding.editTextPassword.text.toString())
            viewModel.checkDomain(request, resources.configuration.locale.language)

        }

        viewModel.isCompanyAuthCodeRequired.observe(viewLifecycleOwner) {
            if (it != null){
                if (it) { // TODO: if (it != null && !it) olarak değişecek
                    NavHostFragment.findNavController(this).navigate(R.id.action_registrationFragment_to_emailCodeFragment)
                    viewModel.isCompanyAuthCodeRequired.value = null
                } else
                {
                    NavHostFragment.findNavController(this).navigate(R.id.action_registrationFragment_to_companyCodeFragment)
                    viewModel.isCompanyAuthCodeRequired.value = null
                }
            }
        }

        binding.editTextPassword.onFocusChangeListener = OnFocusChangeListener { view, b ->
            if (b) {
                view.viewTreeObserver.addOnGlobalLayoutListener {
                    val r = Rect()
                    view.getWindowVisibleDisplayFrame(r)

                    if (abs(view.rootView.height - (r.bottom - r.top)) > 100) { // if more than 100 pixels, its probably a keyboard...
                        binding.scrollview.scrollToBottomWithoutFocusChange()
                    }
                }
            }
        }

    }

    private fun ScrollView.scrollToBottomWithoutFocusChange() {
        val lastChild = getChildAt(childCount - 1)
        val bottom = lastChild.bottom + paddingBottom
        val delta = bottom - (scrollY + height)
        smoothScrollBy(0, delta)
    }

    private fun setTextErrors() {

        binding.edittextMail.addTextChangedListener {
            if(it.toString().trim().isValidEmail())
                binding.textInputLayoutEmail.error = null
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

        if (!mTooltipBalloon.isShowing && !(password.length >= 6 && sawDigitLetter && sawLowerLetter && sawUpperLetter))
            mTooltipBalloon.showAsDropDown(binding.layoutLines)

        getCharacterStatus(password)

        if (password.length < 6)
            mTooltipBalloon.getContentView().findViewById<AppCompatImageView>(R.id.imageview_1).setImageResource(R.drawable.ic_check_icon_grey)
        else
            mTooltipBalloon.getContentView().findViewById<AppCompatImageView>(R.id.imageview_1).setImageResource(R.drawable.ic_check_icon_green)

        if (!sawUpperLetter)
            mTooltipBalloon.getContentView().findViewById<AppCompatImageView>(R.id.imageview_2).setImageResource(R.drawable.ic_check_icon_grey)
        else
            mTooltipBalloon.getContentView().findViewById<AppCompatImageView>(R.id.imageview_2).setImageResource(R.drawable.ic_check_icon_green)

        if (!sawLowerLetter)
            mTooltipBalloon.getContentView().findViewById<AppCompatImageView>(R.id.imageview_3).setImageResource(R.drawable.ic_check_icon_grey)
        else
            mTooltipBalloon.getContentView().findViewById<AppCompatImageView>(R.id.imageview_3).setImageResource(R.drawable.ic_check_icon_green)

        if (!sawDigitLetter)
            mTooltipBalloon.getContentView().findViewById<AppCompatImageView>(R.id.imageview_4).setImageResource(R.drawable.ic_check_icon_grey)
        else
            mTooltipBalloon.getContentView().findViewById<AppCompatImageView>(R.id.imageview_4).setImageResource(R.drawable.ic_check_icon_green)

        if (password.length >= 6 && sawDigitLetter && sawLowerLetter && sawUpperLetter)
            mTooltipBalloon.dismiss()


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