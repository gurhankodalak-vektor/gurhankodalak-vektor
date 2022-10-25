package com.vektortelekom.android.vservice.ui.carpool

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CarpoolActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.carpool.fragment.*
import javax.inject.Inject


class CarPoolActivity : BaseActivity<CarPoolViewModel>() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolActivityBinding
    var isCalledBefore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<CarpoolActivityBinding>(this, R.layout.carpool_activity)
                .apply {
                    lifecycleOwner = this@CarPoolActivity
                }

        viewModel.getCarpool()

        viewModel.carPoolResponse.observe(this){
            if (it != null && !isCalledBefore){
                isCalledBefore = true
                if (it.response.carPoolPreferences != null && it.response.carPoolPreferences.isRider == true
                    && it.response.ridingWith != null){

                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.carpool_fragment, CarPoolMatchingRiderFragment.newInstance(), CarPoolMatchingRiderFragment.TAG)
                        .commit()


                } else if (it.response.carPoolPreferences != null && it.response.carPoolPreferences.isDriver == true
                    && it.response.approvedRiders != null && it.response.approvedRiders.isNotEmpty()){

                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.carpool_fragment, CarPoolMatchingDriverFragment.newInstance(), CarPoolMatchingDriverFragment.TAG)
                        .commit()

                } else{

                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.carpool_fragment, CarPoolFragment.newInstance(), CarPoolFragment.TAG)
                        .commit()

                }
            }
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