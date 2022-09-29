package com.vektortelekom.android.vservice.ui.registration

import android.os.Bundl
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.RegistrationActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.login.fragment.LoginFragmentFactory
import javax.inject.Inject

class RegistrationActivity : BaseActivity<RegistrationViewModel>(), BaseNavigator  {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    private lateinit var binding: RegistrationActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = LoginFragmentFactory()
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<RegistrationActivityBinding>(this, R.layout.registration_activity).apply {
            lifecycleOwner = this@RegistrationActivity
        }
        viewModel.navigator = this

    }



    override fun getViewModel(): RegistrationViewModel {
        viewModel = ViewModelProvider(this, factory)[RegistrationViewModel::class.java]
        return viewModel
    }
}
