package com.vektortelekom.android.vservice.ui.registration.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.CheckDomainRequest
import com.vektortelekom.android.vservice.data.model.EmailVerifyEmailRequest
import com.vektortelekom.android.vservice.databinding.EmailCodeFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.home.HomeActivity
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
    private lateinit var countDownTimer: CountDownTimer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countDownTimer = object :  CountDownTimer(300000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                var diff = millisUntilFinished
                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60

                val elapsedMinutes = diff / minutesInMilli
                diff %= minutesInMilli

                val elapsedSeconds = diff / secondsInMilli

                val minutes = if (elapsedMinutes.toString().length < 2) {
                    "0".plus(elapsedMinutes)
                } else
                    elapsedMinutes.toString()

                val seconds = if (elapsedSeconds.toString().length < 2) {
                    "0".plus(elapsedSeconds)
                } else
                    elapsedSeconds

                if ((minutes.plus(":").plus(seconds)) == "03:59"){

                    binding.textviewSendAgain.isEnabled = true
                    binding.textviewSendAgain.paintFlags = binding.textviewSendAgain.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    binding.textviewSendAgain.text = activity?.getString(R.string.send_again_enable)
                    binding.textviewSendAgain.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                }

                binding.textviewCountdownTimer.text = minutes.plus(":").plus(seconds)
            }

            override fun onFinish() {
                binding.textviewCountdownTimer.text = "00:00"
                binding.buttonSubmit.isEnabled = false
            }
        }.start()

        binding.textviewSendAgain.setOnClickListener{
            val request = CheckDomainRequest(viewModel.userName, viewModel.userSurname, viewModel.userEmail, viewModel.userPassword)
            viewModel.checkDomain(request, getString(R.string.generic_language))

            refreshCurrentFragment()
        }

        binding.buttonWrongMail.setOnClickListener{
            NavHostFragment.findNavController(this).navigateUp()
        }

        binding.edittextCode.addTextChangedListener {
            if (it != null) {
                binding.buttonSubmit.isEnabled = it.isNotEmpty()
            }
        }

        binding.buttonSubmit.setOnClickListener{
            val request = EmailVerifyEmailRequest(viewModel.userName, viewModel.userSurname, viewModel.userEmail, viewModel.userPassword, viewModel.companyAuthCode.value, binding.edittextCode.text.toString())
            viewModel.verifyEmail(request, getString(R.string.generic_language))
        }
        
        viewModel.isVerifySuccess.observe(viewLifecycleOwner){
            if (it != null && it) {
                stateManager.vektorToken = viewModel.sessionId.value
                countDownTimer.cancel()
            }
            else if (it != null && !it)
                showErrorMessage()
        }

        viewModel.verifyEmailResponse.observe(viewLifecycleOwner){
            if (it.personnel.destination == null || (it != null && it.personnel.destination.id == 0L))
                NavHostFragment.findNavController(this).navigate(R.id.action_emailCodeFragment_to_selectCampusFragment)
            else{
                if(viewModel.surveyQuestionId.value != null){
                    activity?.finish()
                    val intent = Intent(requireActivity(), SurveyActivity::class.java)
                    intent.putExtra("surveyQuestionId", viewModel.surveyQuestionId.value)
                    startActivity(intent)
                } else{
                    activity?.finish()
                    val intent = Intent(requireActivity(), HomeActivity::class.java)
                    intent.putExtra("is_coming_registration", true)
                    startActivity(intent)
                }
            }

        }

    }

    private fun refreshCurrentFragment(){
        val fragmentId = findNavController().currentDestination?.id
        findNavController().popBackStack(fragmentId!!,true)
        findNavController().navigate(fragmentId)
    }

    private fun showErrorMessage(){
        viewModel.isVerifySuccess.value = null

        val builder = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
            .create()
        val view = layoutInflater.inflate(R.layout.message_dialog,null)
        val button = view.findViewById<Button>(R.id.other_button)
        val icon = view.findViewById<AppCompatImageView>(R.id.imageview_icon)
        val title = view.findViewById<TextView>(R.id.textview_subtitle)
        val subTitle = view.findViewById<TextView>(R.id.textview_title)

        subTitle.text = getString(R.string.invalid_code)
        title.text = getString(R.string.invalid_code_text)

        icon.setBackgroundResource(R.drawable.ic_error)

        builder.setView(view)
        button.setOnClickListener {
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()

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