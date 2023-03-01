package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CarpoolMyQrFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import javax.inject.Inject


class CarPoolMyQrFragment : BaseFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding : CarpoolMyQrFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolMyQrFragmentBinding>(inflater, R.layout.carpool_my_qr_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolMyQrFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.back.setOnClickListener {
            activity?.finish()
        }

        viewModel.qrGenerateData?.let {
            setQrData(it)
        } ?: run {
            viewModel.getQrCode()
        }



        viewModel.qrCodeResponse.observe(viewLifecycleOwner){
            if (it != null) {
                setQrData(it)
            }
        }

    }

    private fun setQrData(data: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400)

            binding.qrPageIcon.setImageBitmap(bitmap)
        } catch (e: java.lang.Exception) {
        }
    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CarPoolMyQrFragment"

        fun newInstance(): CarPoolMyQrFragment {
            return CarPoolMyQrFragment()
        }
    }


}