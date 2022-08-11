package com.vektortelekom.android.vservice.ui.poolcar.reservation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektor.ktx.utils.ActivityHelper
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.FlexirideAndPoolcarRequestType
import com.vektortelekom.android.vservice.databinding.PoolCarReservationActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarActivity
import com.vektortelekom.android.vservice.ui.poolcar.intercity.PoolCarIntercityActivity
import com.vektortelekom.android.vservice.ui.poolcar.reservation.fragment.*
import java.util.*
import javax.inject.Inject

class PoolCarReservationActivity: BaseActivity<PoolCarReservationViewModel>(), PoolCarReservationNavigator {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarReservationViewModel

    private lateinit var binding: PoolCarReservationActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<PoolCarReservationActivityBinding>(this, R.layout.pool_car_reservation_activity).apply {
            lifecycleOwner = this@PoolCarReservationActivity
            viewModel = this@PoolCarReservationActivity.viewModel
        }
        viewModel.navigator = this

        if(intent.getBooleanExtra("isAdd", false)) {

            /*if(intent.hasExtra("startTime")) {
                viewModel.selectedStartDate = Date(intent.getLongExtra("startTime", 0))
            }*/

            if(intent.hasExtra("startTime")) {
                val endTime = intent.getLongExtra("startTime", 0)
                viewModel.selectedEndDate = Date(endTime)
                viewModel.selectedStartDate = Date(endTime - 1000 * 60 * 60)

            }

            viewModel.isIntercity = intent.getBooleanExtra("isIntercity", false)

            showPoolCarAddReservationFragment(null)
        }
        else {
            showPoolCarReservationsFragment(null)
        }

        viewModel.selectedReservationToStart.observe(this) { reservation ->
            reservation?.let {
                if (reservation.requestType == FlexirideAndPoolcarRequestType.PARTNER_CAR_REQUEST) {
                    val intent = Intent(this, PoolCarIntercityActivity::class.java)
                    intent.putExtra("startReservation", reservation)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, PoolCarActivity::class.java)
                    intent.putExtra("reservation", reservation)
                    startActivity(intent)
                }
                finish()
            }
        }

    }

    override fun showPoolCarReservationsFragment(view: View?) {

        removeAllFragments()

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarReservationsFragment.newInstance(), PoolCarReservationsFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showPoolCarAddReservationFragment(view: View?) {

        removeAllFragments()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.root_fragment, PoolCarAddReservationFragment.newInstance(), PoolCarAddReservationFragment.TAG)
            .addToBackStack(null)
            .commit()
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

    override fun showSelectToFragment(view: View?) {
        ActivityHelper.hideSoftKeyboard(this)

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarReservationSelectToFragment.newInstance(), PoolCarReservationSelectToFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun updateTo(view: View?) {
        if(supportFragmentManager.findFragmentByTag(PoolCarReservationSelectToFragment.TAG) != null) {
            viewModel.reservationToLocation.value = viewModel.reservationToLocationTemp.value
            viewModel.reservationAddressTextTo.value = viewModel.reservationAddressTextToTemp.value
            onBackPressed()
        }
    }

    override fun showSelectPoiFragment() {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarSelectPoiFragment.newInstance(), PoolCarSelectPoiFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showQrFragment() {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, PoolCarReservationQrFragment.newInstance(), PoolCarReservationQrFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun backToHome(view: View?) {
        finish()
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun onBackPressed() {

        if(supportFragmentManager.findFragmentByTag(PoolCarReservationSelectToFragment.TAG) != null
                || supportFragmentManager.findFragmentByTag(PoolCarSelectPoiFragment.TAG) != null
                || supportFragmentManager.findFragmentByTag(PoolCarReservationQrFragment.TAG) != null) {
            super.onBackPressed()
        }
        else {
            finish()
        }

    }

    private fun removeAllFragments() {
        while(supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }

        val transaction = supportFragmentManager.beginTransaction()

        for(fragment in supportFragmentManager.fragments) {
            transaction.remove(fragment)
        }
        transaction.commit()
    }


    override fun getViewModel(): PoolCarReservationViewModel {
        viewModel = ViewModelProvider(this, factory)[PoolCarReservationViewModel::class.java]
        return viewModel
    }

}