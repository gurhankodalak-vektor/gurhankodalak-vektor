package com.vektortelekom.android.vservice.ui.landing

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.LandingActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import javax.inject.Inject

class LandingActivity : BaseActivity<LandingViewModel>(), LandingNavigator{

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: LandingViewModel

    private lateinit var binding: LandingActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<LandingActivityBinding>(this, R.layout.landing_activity).apply {
            lifecycleOwner = this@LandingActivity
        }
        viewModel.navigator = this

        binding.buttonSignIn.setOnClickListener {
            finishAffinity()
            viewModel.navigator?.showRegisterActivity()
        }

        binding.buttonLogin.setOnClickListener {
            viewModel.navigator?.showLoginActivity()
        }
    }



    override fun getViewModel(): LandingViewModel {
        viewModel = ViewModelProvider(this,factory)[LandingViewModel::class.java]
        return viewModel
    }
}