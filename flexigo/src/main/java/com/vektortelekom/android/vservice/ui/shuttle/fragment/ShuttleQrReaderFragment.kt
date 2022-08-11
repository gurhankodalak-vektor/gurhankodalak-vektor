package com.vektortelekom.android.vservice.ui.shuttle.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.ShuttleQrReaderFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import timber.log.Timber
import javax.inject.Inject

class ShuttleQrReaderFragment : BaseFragment<ShuttleViewModel>(), ZBarScannerView.ResultHandler {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel
    private var mFlashStatus = false
    private var flashActive: Drawable? = null
    private var flashPassive: Drawable? = null

    lateinit var binding : ShuttleQrReaderFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ShuttleQrReaderFragmentBinding>(inflater, R.layout.shuttle_qr_reader_fragment, container, false).apply {
            lifecycleOwner = this@ShuttleQrReaderFragment
            viewModel = this@ShuttleQrReaderFragment.viewModel
        }

        flashActive = resources.getDrawable(R.drawable.icon_active_flash, null)

        flashPassive = resources.getDrawable(R.drawable.icon_passive_flash, null)


        binding.flash.setOnClickListener {
            toggleTorch()
        }

        return binding.root
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
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
        const val TAG: String = "ShuttleQrReaderFragment"

        fun newInstance(): ShuttleQrReaderFragment {
            return ShuttleQrReaderFragment()
        }
    }

    override fun handleResult(rawResult: Result?) {
        activity?.supportFragmentManager?.popBackStack()
        rawResult?.contents?.let {

            try {
                viewModel.readQrCode(it, viewModel.myLocation?.latitude?:0.0, viewModel.myLocation?.longitude?:0.0)
            }
            catch (exception: Exception) {
                viewModel.navigator?.handleError(Exception(getString(R.string.route_not_found)))
                viewModel.isQrCodeOk.value = false
            }

        }
    }

}