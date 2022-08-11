package com.vektortelekom.android.vservice.ui.vanpool.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.PersonsModel
import com.vektortelekom.android.vservice.databinding.VanpoolPassengerBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.vanpool.adapter.VanpoolPassengerAdapter
import javax.inject.Inject

class VanpoolPassengerFragment : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: VanpoolPassengerBinding

    private var vanpoolPassengerAdapter: VanpoolPassengerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<VanpoolPassengerBinding>(inflater, R.layout.vanpool_passenger, container, false).apply {
            lifecycleOwner = this@VanpoolPassengerFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vanpoolPassengerAdapter = VanpoolPassengerAdapter(object : VanpoolPassengerAdapter.VanpoolPassengerItemClickListener{
            override fun onPersonsClick(model: PersonsModel?) {
                if (model != null) {
                    passengerCall(model.phoneNumber!!)
                }
            }

        })

        vanpoolPassengerAdapter?.setPassengerList(viewModel.vanpoolPassengers.value!!)
        binding.recyclerviewPassenger.adapter = vanpoolPassengerAdapter
    }

    fun passengerCall(phoneNumber: String) {
        AppDialog.Builder(requireContext())
                .setCloseButtonVisibility(false)
                .setIconVisibility(false)
                .setTitle(getString(R.string.call_2))
                .setSubtitle(getString(R.string.will_call, phoneNumber))
                .setOkButton(getString(R.string.Generic_Ok)) { d ->
                    d.dismiss()
                    phoneNumber.let {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(it)))
                        startActivity(intent)
                    }

                }
                .setCancelButton(getString(R.string.cancel)) { d ->
                    d.dismiss()
                }
                .create().show()
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "VanpoolPassengerFragment"

        fun newInstance() = VanpoolPassengerFragment()

    }

}