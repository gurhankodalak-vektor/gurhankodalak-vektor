package com.vektortelekom.android.vservice.ui.registration.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CompanyCodeQrReaderFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.registration.RegistrationViewModel
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import timber.log.Timber
import javax.inject.Inject

class CompanyCodeQrReaderFragment: BaseFragment<RegistrationViewModel>(), ZBarScannerView.ResultHandler {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    private var mFlashStatus = false
    private var flashActive: Drawable? = null
    private var flashPassive: Drawable? = null

    private lateinit var binding: CompanyCodeQrReaderFragmentBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CompanyCodeQrReaderFragmentBinding>(inflater, R.layout.company_code_qr_reader_fragment, container, false).apply {
            lifecycleOwner = this@CompanyCodeQrReaderFragment
        }


        val logo = requireActivity().findViewById<View>(R.id.imageview_logo) as AppCompatImageView
        logo.visibility = View.GONE

        flashActive = ContextCompat.getDrawable(requireContext(), R.drawable.icon_active_flash)
        flashPassive = ContextCompat.getDrawable(requireContext(), R.drawable.icon_passive_flash)


        binding.flash.setOnClickListener {
            toggleTorch()
        }

        binding.imageviewClose.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.scannerView.setResultHandler(this)
            binding.scannerView.startCamera()
            binding.scannerView.setAutoFocus(true)
        } else {
            Toast.makeText(requireContext(), getString(R.string.no_camera_permission), Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 0)
                    return
                }
            }

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

    override fun getViewModel(): RegistrationViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RegistrationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CompanyCodeQrReaderFragment"
        fun newInstance() = CompanyCodeQrReaderFragment()

    }

    override fun handleResult(rawResult: Result?) {

        rawResult?.contents?.let { result ->
            try {
                viewModel.companyAuthCode.value = result
                NavHostFragment.findNavController(this).navigateUp()
            }
            catch (exception: Exception) {
                viewModel.navigator?.handleError(Exception(getString(R.string.qr_code_error)))
            }

        }
    }

}