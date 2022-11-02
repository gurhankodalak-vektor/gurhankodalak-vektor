package com.vektortelekom.android.vservice.ui.carpool.dialog

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.ChooseRiderRequest
import com.vektortelekom.android.vservice.data.model.SendOtpRequest
import com.vektortelekom.android.vservice.databinding.SmsCodeOtpFragmentBinding
import com.vektortelekom.android.vservice.ui.BaseDialogFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import javax.inject.Inject

class CarPoolSmsCodeOtpDialog(private val itemId: Long, val user: String, val phoneNumber: String): BaseDialogFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: SmsCodeOtpFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<SmsCodeOtpFragmentBinding>(
            inflater,
            R.layout.sms_code_otp_fragment,
            container,
            false
        ).apply {
            lifecycleOwner = this@CarPoolSmsCodeOtpDialog
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textviewPhone.text = "+".plus(phoneNumber)

        object : CountDownTimer(90000, 1000) {

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

                binding.textviewCountdownTimer.text = minutes.plus(":").plus(seconds)
            }

            override fun onFinish() {
                binding.textviewCountdownTimer.text = "done!"
            }
        }.start()

        binding.buttonPhoneAgain.setOnClickListener {
            dismiss()
        }

        binding.buttonSubmit.setOnClickListener{
            val request = SendOtpRequest(binding.textviewPhone.text.toString(), binding.edittextCode.text.toString())
            viewModel.sendOtpCode(request)
        }

        viewModel.isUpdatedOtp.observe(viewLifecycleOwner){ it ->
            if (it != null && it){

                if (user == "rider"){
                    val request = ChooseRiderRequest(itemId, true, null)
                    viewModel.setChooseRider(request)
                } else{
                    val request = ChooseRiderRequest(itemId, true, null)
                    viewModel.setChooseRider(request)
                }
                parentFragmentManager.findFragmentByTag("CarPoolPhoneNumberDialog")?.let { it1 ->
                    if(it1 is DialogFragment)
                        it1.dismiss()
                }
            }
        }
    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        return viewModel
    }

}