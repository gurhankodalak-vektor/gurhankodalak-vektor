package com.vektortelekom.android.vservice.ui.carpool

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CarpoolActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import javax.inject.Inject

class CarPoolActivity : BaseActivity<CarPoolViewModel>() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<CarpoolActivityBinding>(this, R.layout.carpool_activity).apply {
            lifecycleOwner = this@CarPoolActivity
        }


    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = ViewModelProvider(this, factory)[CarPoolViewModel::class.java]
        return viewModel
    }

    companion object {
        const val TAG: String = "CarPoolActivity"
        fun newInstance() = CarPoolActivity()
    }

}