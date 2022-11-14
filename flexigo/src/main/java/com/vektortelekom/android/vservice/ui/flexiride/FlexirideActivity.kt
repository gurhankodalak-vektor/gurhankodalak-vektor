package com.vektortelekom.android.vservice.ui.flexiride

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.FlexirideActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.flexiride.fragment.*
import java.util.*
import javax.inject.Inject

class FlexirideActivity: BaseActivity<FlexirideViewModel>(), FlexirideNavigator {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: FlexirideViewModel

    private lateinit var binding: FlexirideActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<FlexirideActivityBinding>(this, R.layout.flexiride_activity).apply {
            lifecycleOwner = this@FlexirideActivity
            viewModel = this@FlexirideActivity.viewModel
        }
        viewModel.navigator = this


        binding.textviewTitle.text = getString(R.string.flexiride)

        if(intent != null && intent.getBooleanExtra("is_list", false)) {
            showFlexirideListFragment(null)
        }
        else {

            viewModel.type = if(intent != null && intent.getIntExtra("type", 0) == 1) FlexirideViewModel.FlexirideCreateType.GUEST else FlexirideViewModel.FlexirideCreateType.NORMAL

            val startTime = intent?.getLongExtra("startTime", 0L)
            startTime?.let {
                if(it != 0L) {
                    viewModel.selectedDate = Date(it)
                }
            }
            val location = intent?.getStringExtra("location")
            location?.let {
                val geoCoder = Geocoder(this, Locale(resources.configuration.locale.language))
                try{
                    val addresses = geoCoder.getFromLocationName(it, 1)
                    if(addresses.isNotEmpty()) {
                        val address = addresses[0]

                        viewModel.addressTextTo = address.getAddressLine(0)
                        viewModel.toLocation.value = LatLng(address.latitude, address.longitude)

                    }
                }
                catch (e: Exception) { }


            }
            showFlexirideFromFragment(null)
        }

    }

    override fun showFlexirideFromFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.root_fragment, FlexirideFromFragment.newInstance(), FlexirideFromFragment.TAG)
                .commit()
    }

    override fun showFlexiridePlannedFragment(view: View?) {
        binding.layoutToolbar.visibility = View.GONE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, FlexiRideRequestDetailFragment.newInstance(), FlexiRideRequestDetailFragment.TAG)
                .commit()
    }

    override fun showFlexirideSurveyFragment() {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, FlexirideSurveyFragment.newInstance(), FlexirideSurveyFragment.TAG)
                .commit()
    }

    override fun finishSurvey(view: View?) {
        finish()
    }

    override fun showUnauthorizedMessage() {
        FlexigoInfoDialog.Builder(this)
                .setIconVisibility(false)
                .setCancelable(false)
                .setTitle(getString(R.string.pool_car_unauthorized_title))
                .setText1(getString(R.string.pool_car_unauthorized_text))
                .setOkButton(getString(R.string.Generic_Ok)) {
                    finish()
                }
                .create()
                .show()
    }

    override fun showFlexirideListFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.root_fragment, FlexirideListFragment.newInstance(), FlexirideListFragment.TAG)
                .commit()
    }

    override fun showFlexirideSearchFromFragment(view: View?) {
        binding.textviewTitle.text = getString(R.string.search_address)
        viewModel.isFrom = true
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, FlexirideSearchFromFragment.newInstance(), FlexirideSearchFromFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showFlexirideSearchToFragment(view: View?) {
        viewModel.isFrom = false
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, FlexirideSearchFromFragment.newInstance(), FlexirideSearchFromFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun confirmAddress(view: View?) {
        if(viewModel.fromLocation.value != null && viewModel.toLocation.value != null) {
            val fragment = supportFragmentManager.findFragmentByTag(FlexirideFromFragment.TAG)
            if(fragment is FlexirideFromFragment) {
                fragment.continueAfterFromToSelected()
            }
        }
        else {
            if(viewModel.isFromAlreadySelected) {
                FlexigoInfoDialog.Builder(this)
                        .setText1(getString(R.string.please_select_campus))
                        .setCancelable(true)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
            }
            else {
                viewModel.isFromAlreadySelected = true

                showFlexirideSearchToFragment(view)
            }
        }
    }

    override fun getViewModel(): FlexirideViewModel {
        viewModel = ViewModelProvider(this, factory)[FlexirideViewModel::class.java]
        return viewModel
    }

    override fun backPressed(view: View?) {
        binding.textviewTitle.text = getString(R.string.flexiride)
        onBackPressed()
    }

}