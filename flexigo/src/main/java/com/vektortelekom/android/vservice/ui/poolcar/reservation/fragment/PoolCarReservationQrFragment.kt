package com.vektortelekom.android.vservice.ui.poolcar.reservation.fragment

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
import com.vektortelekom.android.vservice.databinding.PoolCarReservationQrFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationViewModel
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import timber.log.Timber
import javax.inject.Inject

class PoolCarReservationQrFragment: BaseFragment<PoolCarReservationViewModel>(), ZBarScannerView.ResultHandler {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarReservationViewModel

    private lateinit var binding: PoolCarReservationQrFragmentBinding

    private var mFlashStatus = false
    private var flashActive: Drawable? = null
    private var flashPassive: Drawable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate<PoolCarReservationQrFragmentBinding>(inflater, R.layout.pool_car_reservation_qr_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarReservationQrFragment
            viewModel = this@PoolCarReservationQrFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        flashActive = resources.getDrawable(R.drawable.icon_active_flash, null)

        flashPassive = resources.getDrawable(R.drawable.icon_passive_flash, null)


        binding.flash.setOnClickListener {
            toggleTorch()
        }

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

    override fun handleResult(rawResult: Result?) {
        activity?.supportFragmentManager?.popBackStack()
        rawResult?.contents?.let {

            viewModel.updateReservationVehicleWithQr(it)

        }
    }

    override fun getViewModel(): PoolCarReservationViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarReservationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarReservationQrFragment"

        fun newInstance() = PoolCarReservationQrFragment()

    }

}