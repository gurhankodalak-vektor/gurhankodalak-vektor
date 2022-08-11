package com.vektortelekom.android.vservice.ui.taxi

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.TaxiActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.taxi.fragment.*
import javax.inject.Inject

class TaxiActivity: BaseActivity<TaxiViewModel>(), TaxiNavigator {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: TaxiViewModel

    private lateinit var binding: TaxiActivityBinding

    var type = 1

    private var prevLocation: LatLng? = null
    private var prevLocationText: String? = null
    private var isCancelSelectLocation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<TaxiActivityBinding>(this, R.layout.taxi_activity).apply {
            lifecycleOwner = this@TaxiActivity
            viewModel = this@TaxiActivity.viewModel
        }
        viewModel.navigator = this

        if(intent != null) {
            type = intent.getIntExtra("type", 1)
        }

        when(type) {
            1 -> {
                viewModel.getPersonnelInfo()
            }
            2 -> {
                showTaxiReportFragment(null)
            }
            3 -> {
                showTaxiListFragment(null)
            }
        }

        viewModel.taxiUsage.observe(this) { taxiUsage ->
            if (taxiUsage == null) {
                showTaxiStartFragment(null)
            } else {
                showTaxiFinishFragment(null)
            }
        }

        viewModel.taxiUsageStarted.observe(this) {
            FlexigoInfoDialog.Builder(this)
                    .setIconVisibility(false)
                    .setCancelable(true)
                    .setText1(getString(R.string.taxi_usage_started))
                    .setOkButton(getString(R.string.Generic_Ok)) {
                        it.dismiss()
                    }
                    .create()
                    .show()

        }

        viewModel.taxiUsageFinished.observe(this) {
            FlexigoInfoDialog.Builder(this)
                    .setIconVisibility(false)
                    .setCancelable(false)
                    .setText1(getString(R.string.taxi_usage_finished))
                    .setOkButton(getString(R.string.Generic_Ok)) {
                        it.dismiss()
                        finish()
                    }
                    .create()
                    .show()
        }

    }

    override fun showTaxiStartFragment(view: View?) {

        binding.textViewToolbarTitle.text = getString(R.string.live_taxi_usage_title)

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, TaxiStartFragment.newInstance(), TaxiStartFragment.TAG)
                .commit()
    }

    override fun showTaxiFinishFragment(view: View?) {

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, TaxiFinishFragment.newInstance(), TaxiFinishFragment.TAG)
                .commit()
    }

    override fun showTaxiReportFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, TaxiReportFragment.newInstance(), TaxiReportFragment.TAG)
                .commit()
    }

    override fun showTaxiListFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, TaxiListFragment.newInstance(), TaxiListFragment.TAG)
                .commit()
    }

    override fun showReportStartLocationFragment(view: View?) {
        isCancelSelectLocation = true
        prevLocation = viewModel.startLocationReport.value
        prevLocationText = viewModel.startLocationTextReport.value
        val fragment = TaxiFindLocationFragment.newInstance()
        val bundle = Bundle()
        bundle.putInt("is_start", 1)
        bundle.putInt("is_report", 1)
        viewModel.isStart = true
        viewModel.isReport = 1
        fragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, TaxiFindLocationFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showReportEndLocationFragment(view: View?) {
        isCancelSelectLocation = true
        prevLocation = viewModel.endLocationReport.value
        prevLocationText = viewModel.endLocationTextReport.value
        val fragment = TaxiFindLocationFragment.newInstance()
        val bundle = Bundle()
        bundle.putInt("is_start", 0)
        bundle.putInt("is_report", 1)
        viewModel.isStart = false
        viewModel.isReport = 1
        fragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, TaxiFindLocationFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showFinishEndLocationFragment(view: View?) {
        isCancelSelectLocation = true
        prevLocation = viewModel.endLocationReport.value
        prevLocationText = viewModel.endLocationTextReport.value
        val fragment = TaxiFindLocationFragment.newInstance()
        val bundle = Bundle()
        bundle.putInt("is_start", 0)
        bundle.putInt("is_report", 2)
        viewModel.isStart = false
        viewModel.isReport = 2
        fragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, TaxiFindLocationFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showStartStartLocationFragment(view: View?) {
        isCancelSelectLocation = true
        prevLocation = viewModel.startLocationStart.value
        prevLocationText = viewModel.startLocationTextStart.value
        val fragment = TaxiFindLocationFragment.newInstance()
        val bundle = Bundle()
        bundle.putInt("is_start", 1)
        bundle.putInt("is_report", 0)
        viewModel.isStart = true
        viewModel.isReport = 0
        fragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, TaxiFindLocationFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    /*override fun showStartEndLocationFragment(view: View?) {
        isCancelSelectLocation = true
        prevLocation = viewModel.endLocationStart.value
        prevLocationText = viewModel.endLocationTextStart.value
        val fragment = TaxiFindLocationFragment.newInstance()
        val bundle = Bundle()
        bundle.putInt("is_start", 0)
        bundle.putInt("is_report", 0)
        viewModel.isStart = false
        viewModel.isReport = false
        fragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, TaxiFindLocationFragment.TAG)
                .addToBackStack(null)
                .commit()
    }*/

    override fun updateAddress(view: View?) {
        if(supportFragmentManager.findFragmentByTag(TaxiFindLocationFragment.TAG) != null) {
            isCancelSelectLocation = false
            onBackPressed()
        }
    }

    override fun getViewModel(): TaxiViewModel {
        viewModel = ViewModelProvider(this, factory)[TaxiViewModel::class.java]
        return viewModel
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun onBackPressed() {
        if(isCancelSelectLocation && supportFragmentManager.findFragmentByTag(TaxiFindLocationFragment.TAG) != null) {

            when(viewModel.isReport) {
                0 -> {
                    if(viewModel.isStart) {
                        viewModel.isStartLocationChangedManuelly = false
                        viewModel.startLocationStart.value = prevLocation
                        viewModel.startLocationTextStart.value = prevLocationText
                    }
                }
                1 -> {
                    if(viewModel.isStart) {
                        viewModel.startLocationReport.value = prevLocation
                        viewModel.startLocationTextReport.value = prevLocationText
                    }
                    else {
                        viewModel.endLocationReport.value = prevLocation
                        viewModel.endLocationTextReport.value = prevLocationText
                    }
                }
                2 -> {
                    if(viewModel.isStart.not()) {
                        viewModel.isEndLocationChangedManuelly = false
                        viewModel.endLocationFinish.value = prevLocation
                        viewModel.endLocationTextFinish.value = prevLocationText
                    }
                }
            }
        }
        super.onBackPressed()
    }

}