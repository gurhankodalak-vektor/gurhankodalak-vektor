package com.vektortelekom.android.vservice.ui.home.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
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
import com.vektortelekom.android.vservice.databinding.QrReaderFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.home.HomeViewModel
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleActivity
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import timber.log.Timber
import javax.inject.Inject

class ScanQrReaderFragment : BaseFragment<HomeViewModel>(), ZBarScannerView.ResultHandler {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: HomeViewModel
    private var mFlashStatus = false
    private var flashActive: Drawable? = null
    private var flashPassive: Drawable? = null

    lateinit var binding : QrReaderFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<QrReaderFragmentBinding>(inflater, R.layout.qr_reader_fragment, container, false).apply {
            lifecycleOwner = this@ScanQrReaderFragment
        }

        flashActive = ContextCompat.getDrawable(requireContext(), R.drawable.icon_active_flash)
        flashPassive = ContextCompat.getDrawable(requireContext(), R.drawable.icon_passive_flash)

        binding.imageviewClose.setOnClickListener{
            activity?.finish()
        }

        binding.flash.setOnClickListener {
            toggleTorch()
        }

        viewModel.isQrCodeOk.observe(viewLifecycleOwner){
            if (it != null && it) {
                showSuccessfulDialog()
            }
        }

        viewModel.errorMessageQrCode.observe(viewLifecycleOwner){
            if (it != null)
                showErrorMessage(it)
        }

        return binding.root
    }

    private fun showErrorMessage(message: String) {
        val dialog = AppDialog.Builder(requireContext())
            .setIconVisibility(true)
            .setTitle(getString(R.string.Generic_Err))
            .setSubtitle(message)
            .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                dialog.dismiss()
                onResume()
            }
            .create()

        dialog.show()
    }

    private fun showSuccessfulDialog() {

        val dialog = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.setCancelable(false)
        dialog.setMessage(resources.getString(R.string.qr_code_successfull))
        dialog.setNeutralButton(resources.getString(R.string.Generic_Ok)) { d, _ ->
            if (viewModel.isCameNotification){
                activity?.finish()
                d.dismiss()

                showShuttleActivity()
            } else{
                activity?.finish()
                d.dismiss()
            }

        }

        dialog.show()

    }

    private fun showShuttleActivity() {
        val intent = Intent(requireActivity(), ShuttleActivity::class.java)
        startActivity(intent)
    }

    override fun getViewModel(): HomeViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[HomeViewModel::class.java] }
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
        const val TAG: String = "ScanQrReaderFragment"

        fun newInstance(): ScanQrReaderFragment {
            return ScanQrReaderFragment()
        }
    }

    override fun handleResult(rawResult: Result?) {
        rawResult?.contents?.let {

            try {
                if (it.contains("{")){
                    val model = ResponseModel(it)
                    viewModel.readQrCodeCarpool(model)
                } else{
                    viewModel.readQrCodeShuttle(it, viewModel.myLocation?.latitude?:0.0, viewModel.myLocation?.longitude?:0.0)
                }

            }
            catch (exception: Exception) {
                viewModel.navigator?.handleError(Exception(getString(R.string.route_not_found)))
            }

        }
    }

}