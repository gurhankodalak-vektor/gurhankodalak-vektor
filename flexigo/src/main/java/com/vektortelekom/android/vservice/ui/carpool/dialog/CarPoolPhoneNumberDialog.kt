package com.vektortelekom.android.vservice.ui.carpool.dialog

import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CarpoolPhoneNumberFragmentBinding
import com.vektortelekom.android.vservice.ui.BaseDialogFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.CustomCountryListAdapter
import javax.inject.Inject

class CarPoolPhoneNumberDialog(private val itemId: Long, val user: String, val listener: PhoneNumberClickListener): BaseDialogFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolPhoneNumberFragmentBinding

    var adapter : CustomCountryListAdapter? = null

    private var countryCode : String? = null
    var characterCount : Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<CarpoolPhoneNumberFragmentBinding>(
            inflater,
            R.layout.carpool_phone_number_fragment,
            container,
            false
        ).apply {
            lifecycleOwner = this@CarPoolPhoneNumberDialog
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val phoneUtil = PhoneNumberUtil.getInstance()

        viewModel.getCountryCode()

        viewModel.countryCode.observe(viewLifecycleOwner){
            adapter = CustomCountryListAdapter(requireContext(), R.layout.textview, it)
            binding.autoCompleteTextView.setAdapter(adapter)

            viewModel.areaCode.value = it.first().areaCode
            binding.autoCompleteTextView.setText("+ ".plus(it.first().areaCode))
            binding.autoCompleteTextView.inputType = InputType.TYPE_NULL
        }

        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val item = "+".plus(adapter?.getItem(position)?.areaCode)
            binding.autoCompleteTextView.setText(item)

            viewModel.areaCode.value = adapter?.getItem(position)?.areaCode
            binding.edittextPhoneNumber.setText("")
        }

        viewModel.areaCode.observe(viewLifecycleOwner){
            if (it != null){
                try {
                    countryCode = phoneUtil.getRegionCodesForCountryCode(it.toInt()).first()

                    val nationalNumber = phoneUtil.getInvalidExampleNumber(countryCode.toString()).nationalNumber
                    characterCount = nationalNumber.toString().length + 1

                    binding.edittextPhoneNumber.hint = nationalNumber.toString()
                    binding.edittextPhoneNumber.filters = arrayOf<InputFilter>(
                        LengthFilter(characterCount!!)
                    )

                } catch (e: NumberParseException) {
                    System.err.println("NumberParseException was thrown: $e")
                }
            }
        }

        binding.buttonSend.setOnClickListener{
            listener.sendClick("+".plus(viewModel.areaCode.value).plus(binding.edittextPhoneNumber.text.toString()))
        }

        viewModel.isUpdatedPhoneNumber.observe(viewLifecycleOwner){
            if (it != null && it){
                showOtpDialog(viewModel.areaCode.value.plus(binding.edittextPhoneNumber.text.toString()))
            }
        }

    }

    private fun showOtpDialog(phoneNumber: String) {

        val carPoolSmsCodeOtpDialog = CarPoolSmsCodeOtpDialog(itemId, user, phoneNumber)

        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.addToBackStack(null)
        carPoolSmsCodeOtpDialog.setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        carPoolSmsCodeOtpDialog.show(ft, "CarPoolSmsCodeOtpDialog")

        childFragmentManager.executePendingTransactions()

    }

    interface PhoneNumberClickListener {
        fun sendClick(phoneNumber: String)
    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }
}