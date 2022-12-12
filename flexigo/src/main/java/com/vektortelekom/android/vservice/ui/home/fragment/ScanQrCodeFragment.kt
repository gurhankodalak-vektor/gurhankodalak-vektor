package com.vektortelekom.android.vservice.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.ScanQrCodeFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.home.HomeViewModel
import javax.inject.Inject

class ScanQrCodeFragment : BaseFragment<HomeViewModel>(),
    PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: HomeViewModel

    lateinit var binding: ScanQrCodeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ScanQrCodeFragmentBinding>(inflater, R.layout.scan_qr_code_fragment, container, false).apply {
            lifecycleOwner = this@ScanQrCodeFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchQr.isChecked = AppDataManager.instance.isQrAutoOpen

        binding.buttonScanQrCode.setOnClickListener {
            if(requireActivity() is BaseActivity<*> && (requireActivity() as BaseActivity<*>).checkAndRequestCameraPermission(this@ScanQrCodeFragment)) {
                onCameraPermissionOk()
            }
        }

        binding.switchQr.setOnCheckedChangeListener { _, isChecked ->
            AppDataManager.instance.isQrAutoOpen = isChecked
        }

    }
    override fun onCameraPermissionFailed() {}

    override fun onCameraPermissionOk() {
        parentFragmentManager
            .beginTransaction()
            .add(
                R.id.qr_code_fragment,
                ScanQrReaderFragment.newInstance(),
                ScanQrReaderFragment.TAG
            )
            .addToBackStack(null)
            .commit()
    }



    override fun getViewModel(): HomeViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[HomeViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "ScanQrCodeFragment"
        fun newInstance() = ScanQrCodeFragment()

    }

}