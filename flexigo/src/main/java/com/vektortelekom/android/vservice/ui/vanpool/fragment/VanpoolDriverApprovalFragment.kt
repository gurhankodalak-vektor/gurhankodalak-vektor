package com.vektortelekom.android.vservice.ui.vanpool.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.VanpoolDriverBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.home.HomeViewModel
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.bitmapDescriptorFromVector
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import com.vektortelekom.android.vservice.utils.dpToPx
import javax.inject.Inject


class VanpoolDriverApprovalFragment: BaseFragment<HomeViewModel>(){

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: HomeViewModel

    lateinit var binding: VanpoolDriverBinding

    private var workplaceIcon: BitmapDescriptor? = null
    private var homeIcon: BitmapDescriptor? = null
    private var stationIcon: BitmapDescriptor? = null

    private var googleMap: GoogleMap? = null
    private var campusId: Long? = null
    private var destinationId: LocationModel? = null
    private var isAcceptedDriver: Boolean? = null
    private var positiveMessage: String? = ""
    private var negativeMessage: String? = ""
    private var titleMessage: String? = ""

    private lateinit var bottomSheetBehaviorEditShuttle: BottomSheetBehavior<*>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<VanpoolDriverBinding>(inflater, R.layout.vanpool_driver, container, false).apply {
            lifecycleOwner = this@VanpoolDriverApprovalFragment
            viewModel = this@VanpoolDriverApprovalFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            googleMap = it

            workplaceIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_workplace)
            stationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_station)
            homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)

            googleMap?.setInfoWindowAdapter(ShuttleInfoWindowAdapter(requireActivity()))

        }

        binding.mapView.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels - 407f.dpToPx(requireContext())

        bottomSheetBehaviorEditShuttle = BottomSheetBehavior.from(binding.bottomSheetRoutePreview)
        bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetBehaviorEditShuttle.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomSheetBehaviorEditShuttle.state = BottomSheetBehavior.STATE_COLLAPSED
            }

        })

        viewModel.instanceId.value?.let { viewModel.getWorkgroupInformation(it) }
        viewModel.versionedRouteId.value?.let { viewModel.getDraftRouteDetails(it) }

        viewModel.workgroupInfo.observe(requireActivity()) {
            if (it != null) {
                viewModel.textviewVanpoolRouteName.value = it.instance.name ?: ""
                viewModel.textviewVanpoolDepartureFromStop.value = it.template.shift?.departureHour.convertHourMinutes()
                        ?: it.template.shift?.arrivalHour.convertHourMinutes()
                viewModel.textviewVanpoolDepartureFromCampus.value = it.template.shift?.returnDepartureHour.convertHourMinutes()
                        ?: ""

                campusId = viewModel.workgroupInfo.value?.template?.fromTerminalReferenceId
                        ?: viewModel.workgroupInfo.value?.template?.toTerminalReferenceId ?: 0
                viewModel.getCampusInfo(campusId!!)
            }
        }
        setApprovalTypeForScreen()

        viewModel.campusInfo.observe(requireActivity()) {
            if (it != null) {
                destinationId = it.location
                it.location?.let { it1 -> fillDestination(it1) }
            }
        }

        viewModel.isUpdateSuccess.observe(requireActivity()) {
            if (it != null) {
                if (isAcceptedDriver == true) {
                    if (viewModel.approvalType.value == VanpoolApprovalType.VANPOOL_DRIVER) {
                        val dialog = AppDialog.Builder(requireContext())
                                .setIconVisibility(false)
                                .setTitle(titleMessage!!)
                                .setSubtitle(positiveMessage!!)
                                .setOkButton(resources.getString(R.string.add_now)) { dialog ->
                                    dialog.dismiss()
                                    viewModel.isForDrivingLicence.value = true
                                }
                                .setCancelButton(resources.getString(R.string.add_later)) { dialog ->
                                    dialog.dismiss()
                                    activity?.finish()
                                }
                                .create()

                        dialog.show()
                    } else {
                        val dialog = AppDialog.Builder(requireContext())
                                .setIconVisibility(false)
                                .setTitle(titleMessage!!)
                                .setSubtitle(positiveMessage!!)
                                .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                                    dialog.dismiss()
                                    activity?.finish()
                                }
                                .create()

                        dialog.show()
                    }

                } else {

                    if (viewModel.approvalType.value == VanpoolApprovalType.VANPOOL_DRIVER) {
                        val dialog = AppDialog.Builder(requireContext())
                            .setIconVisibility(false)
                            .setTitle(getString(R.string.attendence_success_message_title))
                            .setSubtitle(getString(R.string.attendence_success_message))
                            .setOkButton(resources.getString(R.string.add_now)) { dialog ->
                                dialog.dismiss()
                                viewModel.isForDrivingLicence.value = true
                            }
                            .setCancelButton(resources.getString(R.string.add_later)) { dialog ->
                                dialog.dismiss()
                                activity?.finish()
                            }
                            .create()

                        dialog.show()
                    } else {
                        val dialog = AppDialog.Builder(requireContext())
                            .setIconVisibility(false)
                            .setTitle(negativeMessage!!)
                            .setOkButton(resources.getString(R.string.Generic_Close)) { dialog ->
                                dialog.dismiss()
                                activity?.finish()
                            }
                            .create()

                        dialog.show()
                    }

                }
            }
        }

        viewModel.draftRouteDetails.observe(requireActivity()) {
            if (it != null) {
                viewModel.textviewVanpoolStationName.value = it.stations.first().name ?: ""
                viewModel.textviewVanpoolWalkingDistance.value = "100m"

                fillUI(it)
            }
        }


        binding.checkboxTerms.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                binding.buttonAccept.isEnabled = true
                binding.buttonAcceptRider.isEnabled = true
                binding.buttonReject.isEnabled = true
            } else{
                binding.buttonAccept.isEnabled = false
                binding.buttonAcceptRider.isEnabled = false
                binding.buttonReject.isEnabled = false
            }
        }

        binding.buttonCurrentLocation.setOnClickListener {
            viewModel.myLocation?.let {
                val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                googleMap?.animateCamera(cu)
            }
        }
        binding.buttonHomeLocation.setOnClickListener {
            val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation
            homeLocation?.let {
                val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                googleMap?.animateCamera(cu)
            }
        }
        binding.buttonCampusLocation.setOnClickListener {
            destinationId?.let {
                val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                googleMap?.animateCamera(cu)
            }
        }

        binding.buttonAccept.setOnClickListener {
            isAcceptedDriver = true
            viewModel.updateApproval(viewModel.approvalItemId.value!!, "VANPOOL_DRIVER")
        }
        binding.buttonAcceptRider.setOnClickListener {
            isAcceptedDriver = false
            viewModel.updateApproval(viewModel.approvalItemId.value!!, "VANPOOL_USER")
        }
        binding.buttonReject.setOnClickListener {
            isAcceptedDriver = false
            viewModel.updateApproval(viewModel.approvalItemId.value!!, "NONE")
        }


    }
    private fun setApprovalTypeForScreen(){
        when(viewModel.approvalType.value) {
            VanpoolApprovalType.VANPOOL_DRIVER  -> {
                binding.buttonAccept.isEnabled = false
                binding.buttonAcceptRider.isEnabled = false
                binding.buttonReject.isEnabled = false

                binding.buttonReject.visibility = View.VISIBLE
                binding.buttonAcceptRider.visibility = View.VISIBLE
                binding.buttonAccept.visibility = View.VISIBLE
                binding.checkboxTerms.visibility = View.VISIBLE

                positiveMessage = getString(R.string.driver_success_message)
                titleMessage = getString(R.string.driver_success_message_title)
                negativeMessage = getString(R.string.negative_message_title)
            }
            VanpoolApprovalType.VANPOOL_USER -> {

                binding.buttonReject.visibility = View.VISIBLE
                binding.buttonAcceptRider.visibility = View.VISIBLE
                binding.buttonAccept.visibility = View.GONE
                binding.checkboxTerms.visibility = View.GONE

                positiveMessage = getString(R.string.attendence_success_message)
                titleMessage = getString(R.string.attendence_success_message_title)
                negativeMessage = getString(R.string.negative_message_title)
            }
            VanpoolApprovalType.DRIVER_AND_USER -> {

                binding.buttonReject.visibility = View.VISIBLE
                binding.buttonAcceptRider.visibility = View.VISIBLE
                binding.buttonAccept.visibility = View.VISIBLE
                binding.checkboxTerms.visibility = View.GONE

                positiveMessage = getString(R.string.attendence_success_message)
                titleMessage = getString(R.string.attendence_success_message_title)
                negativeMessage = getString(R.string.negative_message_title)
            }
            VanpoolApprovalType.NONE -> {
                binding.buttonReject.visibility = View.VISIBLE
                binding.buttonAcceptRider.visibility = View.VISIBLE
                binding.buttonAccept.visibility = View.VISIBLE
                binding.checkboxTerms.visibility = View.GONE
            }
        }
    }

    private fun fillHomeLocation() {
        val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation

        homeLocation?.let {
            val location = LatLng(homeLocation.latitude, homeLocation.longitude)
            googleMap?.addMarker(MarkerOptions().position(location).icon(homeIcon))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
        }
    }
    private fun fillStations(stations: List<StationModel>) {
        for (station in stations) {
            googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(stationIcon))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(station.location.latitude, station.location.longitude), 14f))
        }
    }

    private fun fillUI(routeDraft: RouteDraftsModel) {
        googleMap?.clear()

        fillHomeLocation()

        routeDraft.stations.let { stations ->
            fillStations(stations)
        }
        routeDraft.draftVersion.routeDetail.path.let {
            fillPath(it.lstTrackPositions)
        }

    }

    private fun fillPath(pointList: List<LstTrackPosition>) {

        if (pointList.isNotEmpty()) {
            val options = PolylineOptions()
            val builder = LatLngBounds.Builder()
            for (z in pointList.indices) {
                val point = LatLng(pointList[z].latitude, pointList[z].longitude)
                builder.include(point)
                options.add(point)
            }
            try {

                val bounds = builder.build()
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                googleMap!!.animateCamera(cu)
            }
            catch (e: Exception) {

            }
            googleMap?.addPolyline(options)

        }
    }

    private fun fillDestination(location: LocationModel) {
        googleMap?.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).icon(workplaceIcon))
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

    override fun getViewModel(): HomeViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[HomeViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "VanpoolDriverApprovalFragment"

        fun newInstance() = VanpoolDriverApprovalFragment()

    }

}