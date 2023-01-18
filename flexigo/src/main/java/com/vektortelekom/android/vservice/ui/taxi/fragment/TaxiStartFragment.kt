package com.vektortelekom.android.vservice.ui.taxi.fragment

import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.TaxiStartFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.taxi.TaxiViewModel
import com.vektortelekom.android.vservice.utils.convertForDay
import com.vektortelekom.android.vservice.utils.convertForMonth
import com.vektortelekom.android.vservice.utils.convertForTicketFullDate
import java.util.*
import javax.inject.Inject

class TaxiStartFragment: BaseFragment<TaxiViewModel>(), PermissionsUtils.LocationStateListener  {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: TaxiViewModel

    lateinit var binding: TaxiStartFragmentBinding

    var selectedDate: Date? = null

    @Volatile
    private var myLocation: Location? = null
    private lateinit var locationClient: FusedLocationClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<TaxiStartFragmentBinding>(inflater, R.layout.taxi_start_fragment, container, false).apply {
            lifecycleOwner = this@TaxiStartFragment
            viewModel = this@TaxiStartFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDate = Date()
        viewModel.selectedDateStart = currentDate
        viewModel.selectedDateTextStart = currentDate.convertForTicketFullDate(requireContext())

        binding.textViewDateFullDate.text = viewModel.selectedDateTextStart
        binding.textViewDateDay.text = currentDate.convertForDay()
        binding.textViewDateMonth.text = currentDate.convertForMonth()

        binding.cardViewDemandDate.setOnClickListener {
            SingleDateAndTimePickerDialog.Builder(context)
                    .defaultDate(viewModel.selectedDateStart?:Date())
                    .bottomSheet()
                    .minutesStep(1)
                    .curved()
                    .todayText(getString(R.string.today))
                    .title(getString(R.string.demand_date))
                    .maxDateRange(Date())
                    .listener { selectedDate ->
                        viewModel.selectedDateStart = selectedDate
                        viewModel.selectedDateTextStart = selectedDate.convertForTicketFullDate(requireContext())

                        binding.textViewDateFullDate.text = viewModel.selectedDateTextStart
                        binding.textViewDateDay.text = selectedDate.convertForDay()
                        binding.textViewDateMonth.text = selectedDate.convertForMonth()
                    }
                    .customLocale(Locale("tr", "TR"))
                    .titleTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
                    .mainColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
                    .display()
        }

        binding.buttonCancel.setOnClickListener {
            activity?.finish()
        }

        if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }

    }

    override fun getViewModel(): TaxiViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[TaxiViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "TaxiStartFragment"
        fun newInstance() = TaxiStartFragment()
    }

    override fun onLocationPermissionOk() {
        locationClient = FusedLocationClient(requireContext())

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            override fun onLocationUpdated(location: Location) {

                myLocation = location
                AppDataManager.instance.currentLocation = location
                if(viewModel.isStartLocationChangedManuelly.not())  {
                    viewModel.startLocationStart.value = LatLng(location.latitude, location.longitude)

                    val geoCoder = Geocoder(requireContext(), Locale("tr-TR"))

                    try {
                        val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)

                        if(addresses.size > 0) {
                            val address = addresses[0]

                            viewModel.startLocationTextStart.value = address.getAddressLine(0)

                        }
                    }
                    catch (e: Exception) {
                        viewModel.startLocationTextStart.value = ""
                    }
                }

                locationClient.stop()


            }

            override fun onLocationFailed(message: String) {
                if(activity?.isFinishing != false || activity?.isDestroyed != false) {
                    return
                }

                when (message) {
                    FusedLocationClient.ERROR_LOCATION_DISABLED -> locationClient.showLocationSettingsDialog()
                    FusedLocationClient.ERROR_LOCATION_MODE -> {
                        locationClient.showLocationSettingsDialog()
                    }
                    FusedLocationClient.ERROR_TIMEOUT_OCCURRED -> {
                        (activity as BaseActivity<*>).handleError(RuntimeException(getString(R.string.location_timeout)))
                    }
                }
            }

        })
    }

    override fun onLocationPermissionFailed() {
    }

}