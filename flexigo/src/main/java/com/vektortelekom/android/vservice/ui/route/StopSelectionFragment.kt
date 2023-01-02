package com.vektortelekom.android.vservice.ui.route

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.StopSelectionFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleActivity
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.utils.*
import javax.inject.Inject

class StopSelectionFragment : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: StopSelectionFragmentBinding

    private var googleMap: GoogleMap? = null

    private var destinationLatLng: LatLng? = null

    private var selectedStation: StationModel? = null

    private var stationIcon: BitmapDescriptor? = null
    private var myStationIcon: BitmapDescriptor? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<StopSelectionFragmentBinding>(inflater, R.layout.stop_selection_fragment, container, false).apply {
            lifecycleOwner = this@StopSelectionFragment
            viewModel = this@StopSelectionFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedStation = viewModel.selectedStation

        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync { map ->
            googleMap = map

            stationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_station)
            myStationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_my_station)
            updateSelectedStop()
            viewModel.selectedRoute?.let { routeModel ->
                viewModel.currentRoute = routeModel
                fillUI(routeModel)
            }

            map.setOnMarkerClickListener { marker ->
                markerClicked(marker)
            }
        }

        binding.buttonCallDriver.setOnClickListener {
            viewModel.selectedRoute?.let { it ->
                val phoneNumber: String = it.driver.phoneNumber

                AppDialog.Builder(requireContext())
                        .setCloseButtonVisibility(false)
                        .setIconVisibility(false)
                        .setTitle(getString(R.string.call_2))
                        .setSubtitle(getString(R.string.will_call, phoneNumber))
                        .setOkButton(getString(R.string.Generic_Ok)) { d ->
                            d.dismiss()

                            phoneNumber.let {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(it)))
                                startActivity(intent)
                            }

                        }
                        .setCancelButton(getString(R.string.cancel)) { d ->
                            d.dismiss()
                        }
                        .create().show()
            }
        }

        binding.buttonStopLocation.setOnClickListener {
            viewModel.navigateToMapTrigger.value = true
        }

        binding.buttonUseIt.setOnClickListener {
            FlexigoInfoDialog.Builder(requireContext())
                    .setTitle(getString(R.string.shuttle_register))
                    .setText1(getString(R.string.shuttle_register_text, viewModel.textViewBottomSheetRoutesTitle.value ?: ""))
                    .setCancelable(false)
                    .setIconVisibility(false)
                    .setOkButton(getString(R.string.confirm)) { dialog ->
                        dialog.dismiss()
                        selectedStation?.let {
                            viewModel.updatePersonnelStation(
                                    id = it.id
                            )
                        }
                    }
                    .setCancelButton(getString(R.string.cancel)) { dialog ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
        }

        viewModel.navigateToMapTrigger.observe(viewLifecycleOwner) {
            if(it != null) {
                viewModel.selectedStation?.let { station ->
                    navigateToMap(viewModel.myLocation?.latitude?:0.0, viewModel.myLocation?.longitude?:0.0, station.location.latitude, station.location.longitude)
                }

                viewModel.navigateToMapTrigger.value = null
            }
        }
        viewModel.updatePersonnelStationResponse.observe(viewLifecycleOwner) {
            if(it != null) {
                viewModel.updatePersonnelStationResponse.value = null
                AppDialog.Builder(requireContext())
                        .setCloseButtonVisibility(false)
                        .setIconVisibility(false)
                        .setSubtitle(getString(R.string.start_route, viewModel.textViewBottomSheetRoutesTitle.value))
                        .setTitle(getString(R.string.congratulations))
                        .setOkButton(getString(R.string.Generic_Ok)) {
                            activity?.finish()
                            showShuttleActivity()
                        }
                        .create().show()


            }
        }

        binding.imageViewBottomSheetRoutesBack.setOnClickListener {
            viewModel.isReturningShuttleEdit = true
            childFragmentManager.popBackStack()
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.remove(this)
                ?.commit()

            viewModel.openRouteSelection.value = true

        }

    }
    private fun showShuttleActivity() {
        val intent = Intent(requireContext(), ShuttleActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMap(userLocationLatitude: Double, userLocationLongitude: Double, targetLocationLatitude: Double, targetLocationLongitude: Double) {
        val baseUri = "geo:%s,%s?q=%s,%s"
        val uri = String.format(baseUri, userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e1: Exception) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(java.lang.String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)))
                    startActivity(browserIntent)
                } catch (e2: java.lang.Exception) {
                    AppLogger.e(e, "NavigationAppNotFound")
                    Toast.makeText(requireContext(), R.string.Maps_No_Exist, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun markerClicked(marker: Marker): Boolean {
        return if (marker.tag is StationModel) {

            val station = marker.tag as StationModel
            viewModel.selectedStation = station
            selectedStation = station
            updateSelectedStop()
            viewModel.currentRoute?.let {
                fillUI(it)
            }

            true
        } else {
            false
        }
    }

    private fun updateSelectedStop() {
        viewModel.selectedStation?.let {
            viewModel.textViewBottomSheetStopName.value = selectedStation?.title
            binding.textviewStopName.text = selectedStation?.title
        }
    }

    private fun fillUI(routeModel: RouteModel) {

        googleMap?.clear()
        val isFirstLeg = viewModel.routeForWorkgroup.value!!.template.direction?.let { viewModel.isFirstLeg(it, viewModel.routeForWorkgroup.value!!.template.fromType!!) } == true

        fillStations(isFirstLeg.let { routeModel.getRoutePath(it) }!!.stations)
        fillPath(isFirstLeg.let { routeModel.getRoutePath(it) }!!.data)

        fillDestination()

    }

    private fun fillPath(pointList: List<List<Double>>) {

        if (pointList.isNotEmpty()) {
            val options = PolylineOptions()
            val firstPoint = pointList[0]
            val lastPoint = pointList[pointList.lastIndex]
            if (lastPoint.size == 2) {
                viewModel.workLocation = LatLng(lastPoint[0], lastPoint[1])
                destinationLatLng = LatLng(lastPoint[0], lastPoint[1])
            }
            if (firstPoint.size == 2) {
                var minLat = firstPoint[0]
                var maxLat = minLat
                var minLng = firstPoint[1]
                var maxLng = minLng
                for (point in pointList) {
                    if (point.size == 2) {
                        val lat = point[0]
                        val lng = point[1]
                        if (lat < minLat) {
                            minLat = lat
                        } else if (lat > maxLat) {
                            maxLat = lat
                        }
                        if (lng < minLng) {
                            minLng = lng
                        } else if (lng > maxLng) {
                            maxLng = lng
                        }
                        options.add(LatLng(lat, lng))
                    }
                }

                try {
                    val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 100)
                    googleMap?.moveCamera(cu)
                }
                catch (e: Exception) {

                }

            }

            googleMap?.addPolyline(options)

        }
    }

    private fun fillStations(stations: List<StationModel>) {
        for (station in stations) {
            val marker: Marker? = if (station.id == selectedStation?.id) {
                googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(myStationIcon))
            } else {
                googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(stationIcon))
            }
            marker?.tag = station

            if(station.id == selectedStation?.id) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(station.location.latitude, station.location.longitude), 14f))
            }

        }
    }

    private fun fillDestination() {
        /*googleMap?.addMarker(MarkerOptions().position(destinationLatLng
                ?: LatLng(0.0, 0.0)).icon(workplaceIcon))*/
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "StopSelectionFragment"
        fun newInstance() = StopSelectionFragment()

    }
}