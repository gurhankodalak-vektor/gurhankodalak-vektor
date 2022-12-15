package com.vektortelekom.android.vservice.ui.registration.fragment

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ScrollView
import androidx.annotation.RequiresApi
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
import com.vektortelekom.android.vservice.utils.tooltip.TooltipBalloonEmail
import javax.inject.Inject


class RegistrationFragment : BaseFragment<RegistrationViewModel>(), TextWatcher,
    OnFocusChangeListener {

    private val mTooltipBalloonPassword by balloon(TooltipBalloon::class)
    private val mTooltipBalloonEmail by balloon(TooltipBalloonEmail::class)

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    lateinit var binding: RegistrationFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<RegistrationFragmentBinding>(
            inflater,
            R.layout.registration_fragment,
            container,
            false
        ).apply {
            lifecycleOwner = this@RegistrationFragment
        }

        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        mTooltipBalloonEmail.bodyWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        super.onViewCreated(view, savedInstanceState)

        setTextErrors()

        binding.editTextPassword.addTextChangedListener(this)

        binding.textviewSignText.setOnClickListener {
            activity?.finish()
        }

        binding.buttonSignup.setOnClickListener {

            viewModel.userName = binding.edittextName.text.toString()
            viewModel.userSurname = binding.edittextSurname.text.toString()
            viewModel.userEmail = binding.edittextMail.text.toString().trim()
            viewModel.userPassword = binding.editTextPassword.text.toString()

            val request = CheckDomainRequest(
                binding.edittextName.text.toString(),
                binding.edittextSurname.text.toString(),
                binding.edittextMail.text.toString().trim(),
                binding.editTextPassword.text.toString()
            )
            viewModel.checkDomain(request, resources.configuration.locale.language)

        }

        viewModel.isCompanyAuthCodeRequired.observe(viewLifecycleOwner) {
            if (it != null && !it) {
                NavHostFragment.findNavController(this).navigate(R.id.action_registrationFragment_to_emailCodeFragment)
                viewModel.isCompanyAuthCodeRequired.value = null
            }
        }

        binding.edittextMail.onFocusChangeListener = this
        binding.editTextPassword.onFocusChangeListener = this
        binding.edittextName.onFocusChangeListener = this
        binding.edittextSurname.onFocusChangeListener = this


        binding.scrollview.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            binding.buttonSignup.getLocationOnScreen(pointSignUp)

            if (((pointSignUp.last() - 200) > pointKeyboard.last()) && pointKeyboard.last() != 0){
                if (mTooltipBalloonEmail.isShowing)
                    mTooltipBalloonEmail.dismiss()
            } else{
                if (!mTooltipBalloonEmail.isShowing && binding.edittextMail.text!!.isEmpty() && hasFocusEmail)
                    mTooltipBalloonEmail.showAsDropDown(binding.edittextMail)
            }

        }

    }

    var keypadHeight : Int = 0
    var screenHeight : Int = 0
    private val pointKeyboard = IntArray(2)
    val pointSignUp = IntArray(2)

    var hasFocusEmail = false

    override fun onFocusChange(v: View, hasFocus: Boolean) {

        when (v.id) {
            R.id.edittext_mail ->{
                if (hasFocus){
                    hasFocusEmail = true

                    if (!mTooltipBalloonEmail.isShowing && binding.edittextMail.text!!.isEmpty())
                        mTooltipBalloonEmail.showAlignBottom(binding.edittextMail)

                    if (mTooltipBalloonPassword.isShowing)
                        mTooltipBalloonPassword.dismiss()

                    v.viewTreeObserver?.addOnGlobalLayoutListener {
                        val r = Rect()
                        v.getWindowVisibleDisplayFrame(r)

                        screenHeight = v.rootView.height
                        keypadHeight = screenHeight - r.bottom

                        if (keypadHeight > screenHeight * 0.15) {

                            v.getLocationOnScreen(pointKeyboard)
                            binding.scrollview.scrollToBottomWithoutFocusChange()

                        }

                    }
                }

            }
            R.id.edit_text_password ->{
                hasFocusEmail = false

                if (hasFocus){
                    if (mTooltipBalloonEmail.isShowing)
                        mTooltipBalloonEmail.dismiss()

                    if (binding.edittextMail.text.toString().trim().isValidEmail())
                        binding.textInputLayoutEmail.error = null
                    else {
                        if (binding.edittextMail.text.toString().isNotEmpty())
                            binding.textInputLayoutEmail.error = getString(R.string.check_information)
                    }

                    if (!mTooltipBalloonPassword.isShowing)
                        mTooltipBalloonPassword.showAsDropDown(binding.editTextPassword)

                    v.viewTreeObserver?.addOnGlobalLayoutListener {

                        val r = Rect()
                        v.getWindowVisibleDisplayFrame(r)

                        val screenHeight: Int = v.rootView.height
                        val keypadHeight = screenHeight - r.bottom

                        if (keypadHeight > screenHeight * 0.15) {
                            v.getLocationOnScreen(pointKeyboard)
                            binding.scrollview.scrollToBottomWithoutFocusChange()

                        }

                    }
                }

            }
            else -> {

                hasFocusEmail = false

                if (mTooltipBalloonEmail.isShowing)
                    mTooltipBalloonEmail.dismiss()

                if (mTooltipBalloonPassword.isShowing)
                    mTooltipBalloonPassword.dismiss()

                if (binding.edittextMail.text.toString().trim().isValidEmail())
                    binding.textInputLayoutEmail.error = null
                else {
                    if (binding.edittextMail.text.toString().isNotEmpty())
                        binding.textInputLayoutEmail.error = getString(R.string.check_information)
                }

                v.viewTreeObserver?.addOnGlobalLayoutListener {
                    val r = Rect()
                    v.getWindowVisibleDisplayFrame(r)

                    screenHeight = v.rootView.height
                    keypadHeight = screenHeight - r.bottom

                    if (keypadHeight > screenHeight * 0.15) {

                        v.getLocationOnScreen(pointKeyboard)
//                        binding.scrollview.scrollToBottomWithoutFocusChange()

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

            if (mTooltipBalloonEmail.isShowing)
                mTooltipBalloonEmail.dismiss()

            binding.textInputLayoutEmail.error = null
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        updatePasswordStrengthView(p0.toString())
    }

    override fun afterTextChanged(p0: Editable?) {}

    private fun updatePasswordStrengthView(password: String) {

//        val strength = PasswordStrength.calculateStrength(password)

//        when (strength.name) {
//            "WEAK" -> {
//                binding.line1.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorPinkishRed
//                    )
//                )
//                binding.line2.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//                binding.line3.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//                binding.line4.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//            }
//            "MEDIUM" -> {
//                binding.line1.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.orangeYellow
//                    )
//                )
//                binding.line2.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.orangeYellow
//                    )
//                )
//                binding.line3.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//                binding.line4.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//            }
//            "STRONG" -> {
//                binding.line1.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.sunflowerYellow
//                    )
//                )
//                binding.line2.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.sunflowerYellow
//                    )
//                )
//                binding.line3.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.sunflowerYellow
//                    )
//                )
//                binding.line4.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//            }
//            "VERY_STRONG" -> {
//                binding.line1.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorAquaGreen
//                    )
//                )
//                binding.line2.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorAquaGreen
//                    )
//                )
//                binding.line3.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorAquaGreen
//                    )
//                )
//                binding.line4.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorAquaGreen
//                    )
//                )
//            }
//            "NONE" -> {
//                binding.line1.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//                binding.line2.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//                binding.line3.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//                binding.line4.setCardBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.colorIceBlue
//                    )
//                )
//            }
//        }

        if (!mTooltipBalloonPassword.isShowing && !(password.length >= 6 && sawDigitLetter && sawLowerLetter && sawUpperLetter))
            mTooltipBalloonPassword.showAsDropDown(binding.editTextPassword)

        getCharacterStatus(password)

        if (password.length < 6)
            mTooltipBalloonPassword.getContentView().findViewById<AppCompatImageView>(R.id.imageview_1)
                .setImageResource(R.drawable.ic_check_icon_grey)
        else
            mTooltipBalloonPassword.getContentView().findViewById<AppCompatImageView>(R.id.imageview_1)
                .setImageResource(R.drawable.ic_check_icon_green)

        if (!sawUpperLetter)
            mTooltipBalloonPassword.getContentView().findViewById<AppCompatImageView>(R.id.imageview_2)
                .setImageResource(R.drawable.ic_check_icon_grey)
        else
            mTooltipBalloonPassword.getContentView().findViewById<AppCompatImageView>(R.id.imageview_2)
                .setImageResource(R.drawable.ic_check_icon_green)

        if (!sawLowerLetter)
            mTooltipBalloonPassword.getContentView().findViewById<AppCompatImageView>(R.id.imageview_3)
                .setImageResource(R.drawable.ic_check_icon_grey)
        else
            mTooltipBalloonPassword.getContentView().findViewById<AppCompatImageView>(R.id.imageview_3)
                .setImageResource(R.drawable.ic_check_icon_green)

        if (!sawDigitLetter)
            mTooltipBalloonPassword.getContentView().findViewById<AppCompatImageView>(R.id.imageview_4)
                .setImageResource(R.drawable.ic_check_icon_grey)
        else
            mTooltipBalloonPassword.getContentView().findViewById<AppCompatImageView>(R.id.imageview_4)
                .setImageResource(R.drawable.ic_check_icon_green)

        if (password.length >= 6 && sawDigitLetter && sawLowerLetter && sawUpperLetter)
            mTooltipBalloonPassword.dismiss()

        if (password.isEmpty())
            mTooltipBalloonPassword.dismiss()

    }

    private var sawUpperLetter = false
    private var sawLowerLetter = false
    private var sawDigitLetter = false

    private fun getCharacterStatus(password: String) {
        var sawUpper = false
        var sawLower = false
        var sawDigit = false

        for (element in password) {

            if (!sawDigit && Character.isDigit(element)) {
                sawDigit = true
            } else {
                if (Character.isUpperCase(element))
                    sawUpper = true
                else if (Character.isLowerCase(element))
                    sawLower = true
            }

        }

        sawUpperLetter = sawUpper
        sawLowerLetter = sawLower
        sawDigitLetter = sawDigit

    }

    override fun getViewModel(): RegistrationViewModel {
        viewModel = activity?.run {
            ViewModelProvider(
                requireActivity(),
                factory
            )[RegistrationViewModel::class.java]
        }
            ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "RegistrationFragment"
        fun newInstance() = RegistrationFragment()
    }


}