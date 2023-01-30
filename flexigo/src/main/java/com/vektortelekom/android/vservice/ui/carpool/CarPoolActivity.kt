package com.vektortelekom.android.vservice.ui.carpool

import android.app.Activity
import android.content.Intent
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
    private var isCalledBefore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        binding = DataBindingUtil.setContentView<CarpoolActivityBinding>(this, R.layout.carpool_activity)
                .apply {
                    lifecycleOwner = this@CarPoolActivity
                }

        viewModel.getCarpool(true)

        viewModel.ridingWith.observe(this){
            if (it != null && viewModel.isRider.value == true){
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.carpool_fragment, CarPoolMatchingRiderFragment.newInstance(), CarPoolMatchingRiderFragment.TAG)
                    .addToBackStack(null)
                    .commit()
            }
        }

        viewModel.approvedRiders.observe(this){
            if (it != null && it.isNotEmpty() && viewModel.isDriver.value == true){
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.carpool_fragment, CarPoolMatchingDriverFragment.newInstance(), CarPoolMatchingDriverFragment.TAG)
                    .addToBackStack(null)
                    .commit()
            }
        }

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
                        .addToBackStack(null)
                        .commit()

                } else{

                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.carpool_fragment, CarPoolFragment.newInstance(), CarPoolFragment.TAG)
                        .addToBackStack(null)
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

    private val CARPOOL_PAGE_CODE = 1001

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CARPOOL_PAGE_CODE) {
            if(resultCode == Activity.RESULT_OK) {

            }
        }
    }

}