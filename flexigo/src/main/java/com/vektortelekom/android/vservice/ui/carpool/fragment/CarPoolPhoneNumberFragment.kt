package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CarpoolPhoneNumberFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.CustomCountryListAdapter
import javax.inject.Inject


class CarPoolPhoneNumberFragment : BaseFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolPhoneNumberFragmentBinding

    var adapter : CustomCountryListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolPhoneNumberFragmentBinding>(inflater, R.layout.carpool_phone_number_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolPhoneNumberFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getCountryCode()

        viewModel.countryCode.observe(viewLifecycleOwner){
            adapter = CustomCountryListAdapter(requireContext(), R.layout.textview, it)
            binding.autoCompleteTextView.setAdapter(adapter)

            binding.autoCompleteTextView.setText("+ ".plus(it.first().areaCode))
            binding.autoCompleteTextView.inputType = InputType.TYPE_NULL
        }

        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val item = "+ ".plus(adapter?.getItem(position)?.areaCode)
            binding.autoCompleteTextView.setText(item)
        }

        // TODO: telefon numarası için karakter sayısı kısıtı eklenebilir.
        binding.buttonSend.isEnabled = viewModel.phoneNumber.value != null

    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CarPoolPhoneNumberFragment"
        fun newInstance() = CarPoolPhoneNumberFragment()
    }


}