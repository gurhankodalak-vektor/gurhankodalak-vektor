package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.ResponseModel
import com.vektortelekom.android.vservice.databinding.CarpoolQrReaderFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import timber.log.Timber
import javax.inject.Inject

class CarPoolQrReaderFragment : BaseFragment<CarPoolViewModel>(), ZBarScannerView.ResultHandler {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel
    private var mFlashStatus = false
    private var flashActive: Drawable? = null
    private var flashPassive: Drawable? = null

    lateinit var binding : CarpoolQrReaderFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolQrReaderFragmentBinding>(inflater, R.layout.carpool_qr_reader_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolQrReaderFragment
        }

        flashActive = ContextCompat.getDrawable(requireContext(), R.drawable.icon_active_flash)
        flashPassive = ContextCompat.getDrawable(requireContext(), R.drawable.icon_passive_flash)

        binding.back.setOnClickListener{
            activity?.finish()
        }

        binding.flash.setOnClickListener {
            toggleTorch()
        }

        viewModel.isReadQrCode.observe(viewLifecycleOwner){
            if (it != null && it)
                activity?.finish()
        }

        return binding.root
    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.scannerView.setResultHandler(this)
            binding.scannerView.startCamera()
            binding.scannerView.setAutoFocus(true)
        } else {
            Toast.makeText(requireContext(), getString(R.string.no_camera_permission), Toast.LENGTH_SHORT).show()
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun toggleTorch() {
        mFlashStatus = !mFlashStatus
        try {
            if (mFlashStatus) {
                binding.flash.setImageDrawable(flashActive)
            } else {
                binding.flash.setImageDrawable(flashPassive)
            }
        } catch (e: java.lang.Exception) {
            Timber.e("Flash icon not changed")
        }
        binding.scannerView.flash = mFlashStatus
    }

    override fun onPause() {
        super.onPause()
        binding.scannerView.stopCamera()
    }

    companion object {
        const val TAG: String = "CarPoolQrReaderFragment"

        fun newInstance(): CarPoolQrReaderFragment {
            return CarPoolQrReaderFragment()
        }
    }

    override fun handleResult(rawResult: Result?) {
        activity?.supportFragmentManager?.popBackStack()
        rawResult?.contents?.let {

            try {
                val model = ResponseModel(it)
                viewModel.readQrCode(model)
            }
            catch (exception: Exception) {
                viewModel.navigator?.handleError(Exception(getString(R.string.route_not_found)))
            }

        }
    }

}