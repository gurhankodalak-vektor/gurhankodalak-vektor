package com.vektortelekom.android.vservice.ui.carpool

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DashboardItemType
import com.vektortelekom.android.vservice.databinding.CarpoolActivityBinding
import com.vektortelekom.android.vservice.databinding.CarpoolQrCodeActivityBinding
import com.vektortelekom.android.vservice.databinding.CarpoolQrReaderFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.carpool.fragment.*
import javax.inject.Inject

class CarPoolQrCodeActivity : BaseActivity<CarPoolViewModel>() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolQrCodeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        binding = DataBindingUtil.setContentView<CarpoolQrCodeActivityBinding>(this, R.layout.carpool_qr_code_activity)
                .apply {
                    lifecycleOwner = this@CarPoolQrCodeActivity
                }

        val type = intent.getStringExtra("type")

        if (type == DashboardItemType.ScanQR.name){

            supportFragmentManager
                .beginTransaction()
                .add(R.id.carpool_qr_code_fragment, CarPoolQrReaderFragment.newInstance(), CarPoolQrReaderFragment.TAG)
                .commit()
        } else
        {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.carpool_qr_code_fragment, CarPoolMyQrFragment.newInstance(), CarPoolMyQrFragment.TAG)
                .commit()
        }



    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = ViewModelProvider(this, factory)[CarPoolViewModel::class.java]
        return viewModel
    }

    companion object {
        const val TAG: String = "CarPoolQrCodeActivity"
        fun newInstance() = CarPoolQrCodeActivity()
    }

}