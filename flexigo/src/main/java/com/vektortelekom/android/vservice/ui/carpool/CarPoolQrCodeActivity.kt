package com.vektortelekom.android.vservice.ui.carpool

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.QrCodeActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.carpool.fragment.*
import javax.inject.Inject

class CarPoolQrCodeActivity : BaseActivity<CarPoolViewModel>() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: QrCodeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        binding = DataBindingUtil.setContentView<QrCodeActivityBinding>(this, R.layout.qr_code_activity)
                .apply {
                    lifecycleOwner = this@CarPoolQrCodeActivity
                }

        supportFragmentManager
            .beginTransaction()
            .add(R.id.qr_code_fragment, CarPoolMyQrFragment.newInstance(), CarPoolMyQrFragment.TAG)
            .commit()

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