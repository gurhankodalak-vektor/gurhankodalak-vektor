package com.vektortelekom.android.vservice.ui.registration

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.RegistrationActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import javax.inject.Inject

class RegistrationActivity : BaseActivity<RegistrationViewModel>(), BaseNavigator  {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    private lateinit var binding: RegistrationActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<RegistrationActivityBinding>(this, R.layout.registration_activity).apply {
            lifecycleOwner = this@RegistrationActivity
        }


        if (AppDataManager.instance.personnelInfo?.destinationId == 0) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.register_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            val navGraph = navController.navInflater.inflate(R.navigation.registration_navigation)
            navGraph.setStartDestination(R.id.selectCampusFragment)
            navController.graph = navGraph
        }

        viewModel.navigator = this


    }

    override fun getViewModel(): RegistrationViewModel {
        viewModel = ViewModelProvider(this, factory)[RegistrationViewModel::class.java]
        return viewModel
    }
}
