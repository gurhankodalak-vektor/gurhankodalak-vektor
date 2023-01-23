package com.vektortelekom.android.vservice.ui.shuttle.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.ShuttleMainFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.comments.CommentsActivity
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.route.search.RouteSearchActivity
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class ShuttleMainFragment : BaseFragment<ShuttleViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: ShuttleMainFragmentBinding

    private var googleMap: GoogleMap? = null

    private var stationIcon: BitmapDescriptor? = null
    private var myStationIcon: BitmapDescriptor? = null
    private var workplaceIcon: BitmapDescriptor? = null
    private var homeIcon: BitmapDescriptor? = null
    private var vehicleIcon: BitmapDescriptor? = null

    private var destinationLatLng: LatLng? = null

    private var vehicleRefreshHandler: Handler? = null
    private var nextRidesRefreshHandler: Handler? = null

    private var lastVehicleUpdateTime = 0L
    private var lastNextRidesUpdateTime = 0L
    private val timeIntervalToUpdateVehicle = 30L * 1000L
    private val timeIntervalToUpdateNextRides = 60L * 1000L

    private var vehicleMarker: Marker? = null

    private lateinit var locationClient: FusedLocationClient

    var selectedDate: Date? = null

    private lateinit var placesClient: PlacesClient

    private var currentRoute: RouteModel? = null

    private var myNextRides = mutableListOf<ShuttleNextRide>()

    private var isVehicleLocationInit = false

    private var cardCurrentRide : ShuttleNextRide? = null

    private var workgroupInstanceIdForVehicle: Long? = null

    private val markerList : MutableList<Marker> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<ShuttleMainFragmentBinding>(inflater, R.layout.shuttle_main_fragment, container, false).apply {
            lifecycleOwner = this@ShuttleMainFragment
            viewModel = this@ShuttleMainFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { it ->

            stationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_station)
            myStationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_my_station)
            workplaceIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_workplace)
            homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)
            vehicleIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_icon_flexishuttle)

            googleMap = it
            googleMap?.setInfoWindowAdapter(ShuttleInfoWindowAdapter(requireActivity()))


            val vehicleLocationResponse = viewModel.vehicleLocation.value
            if (vehicleLocationResponse != null) {
                fillVehicleLocation(vehicleLocationResponse.response)
                lastVehicleUpdateTime = System.currentTimeMillis()
                vehicleRefreshHandler?.removeCallbacksAndMessages(null)
                vehicleRefreshHandler?.postDelayed(vehicleRefreshRunnable, timeIntervalToUpdateVehicle)
            }

            viewModel.routeDetails.observe(viewLifecycleOwner) { routeModel ->
                currentRoute = routeModel
                viewModel.zoomStation = false

                fillUI(routeModel)
            }

            viewModel.fillUITrigger.observe(viewLifecycleOwner) {
                if(it != null) {
                    fillUI(it)
                    viewModel.fillUITrigger.value = null
                }
            }

        }

        viewModel.navigator?.changeTitle(getString(R.string.plan_service))

        viewModel.openBottomSheetEditShuttle.observe(viewLifecycleOwner) {
            if(it != null) {
                vehicleRefreshHandler?.removeCallbacksAndMessages(null)
                nextRidesRefreshHandler?.removeCallbacksAndMessages(null)
            }
        }

        viewModel.clearSelections()

        placesClient = Places.createClient(requireContext())


        viewModel.vehicleLocation.observe(viewLifecycleOwner) { response ->

            if (response != null) {
                if (googleMap != null) {
                    fillVehicleLocation(response.response)
                    lastVehicleUpdateTime = System.currentTimeMillis()
                    vehicleRefreshHandler?.removeCallbacksAndMessages(null)
                    vehicleRefreshHandler?.postDelayed(
                        vehicleRefreshRunnable,
                        timeIntervalToUpdateVehicle
                    )
                }
                if (binding.cardViewRequestStation.visibility == View.VISIBLE) {
                    binding.textViewVehicleError.visibility = View.GONE
                } else {
                    binding.textViewVehicleError.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.vehicleErrorMessage.observe(viewLifecycleOwner) {
            vehicleMarker?.remove()
            vehicleMarker = null
            binding.textViewVehicleError.visibility = View.VISIBLE
            vehicleRefreshHandler?.removeCallbacksAndMessages(null)
            vehicleRefreshHandler?.postDelayed(vehicleRefreshRunnable, timeIntervalToUpdateVehicle)
        }

        binding.buttonCallDriver.setOnClickListener {
            viewModel.routeDetails.value?.let { routeDetails ->
                val phoneNumber: String = routeDetails.driver.phoneNumber

                    AppDialog.Builder(requireContext())
                            .setCloseButtonVisibility(false)
                            .setIconVisibility(false)
                            .setTitle(getString(R.string.call_2))
                            .setSubtitle(getString(R.string.will_call, phoneNumber))
                            .setOkButton(getString(R.string.Generic_Ok)) { d ->
                                d.dismiss()
                                phoneNumber.let {phoneNumber ->
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(phoneNumber)))
                                    startActivity(intent)
                                }

                            }
                            .setCancelButton(getString(R.string.cancel)) { d ->
                                d.dismiss()
                            }
                            .create().show()


            }
        }

        binding.buttonComment.setOnClickListener {
            val intent = Intent(requireActivity(), CommentsActivity::class.java)
            startActivity(intent)
        }

        vehicleRefreshHandler = Handler(requireActivity().mainLooper)
        nextRidesRefreshHandler = Handler(requireActivity().mainLooper)

        if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }
        else {
            onLocationPermissionFailed()
        }

        binding.buttonCurrentLocation.setOnClickListener {
            viewModel.myLocation?.let {
                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f)
                googleMap?.animateCamera(cu)
                googleMap?.moveCamera(cu)
            }
        }

        binding.buttonHomeLocation.setOnClickListener {
            val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation
            homeLocation?.let {
                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f)
                googleMap?.animateCamera(cu)
                googleMap?.moveCamera(cu)

            }
        }

        binding.buttonCampusLocation.setOnClickListener {
            destinationLatLng?.let {
                googleMap?.addMarker(MarkerOptions().position(it).icon(workplaceIcon))
                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f)
                googleMap?.animateCamera(cu)
                googleMap?.moveCamera(cu)
            }
        }

        binding.cardViewShuttleEdit.setOnClickListener {
            viewModel.editClicked.value = true
        }

        binding.imageViewShuttlePrev.setOnClickListener {
            if(viewModel.currentMyRideIndex > 0) {
                viewModel.currentMyRideIndex -= 1
                fillShuttleCardView(myNextRides[viewModel.currentMyRideIndex])
            }
        }

        binding.imageViewShuttleNext.setOnClickListener {
            if(viewModel.currentMyRideIndex < myNextRides.size -1) {
                viewModel.currentMyRideIndex += 1
                fillShuttleCardView(myNextRides[viewModel.currentMyRideIndex])
            }
        }

        viewModel.searchedRoutes.observe(viewLifecycleOwner) { routes ->
            if (routes != null) {
                viewModel.textViewBottomSheetRoutesFromToName.value =
                    (viewModel.selectedFromLocation?.text
                        ?: viewModel.selectedFromDestination?.title)
                        .plus(" - ")
                        .plus(
                            viewModel.selectedToLocation?.text
                                ?: viewModel.selectedToDestination?.title
                        )

                viewModel.searchRoutesAdapterSetListTrigger.value = routes.toMutableList()

                viewModel.isReturningShuttleEdit = true

                viewModel.openBottomSheetRoutes.value = true
            }
        }

        viewModel.getStopsResponse.observe(viewLifecycleOwner) { response ->

            if(response != null) {
                val routeMap = mutableMapOf<Long, StationModel>()
                response.response.forEach { station ->
                    if(routeMap.containsKey(station.routeId)) {
                        val currentStation = routeMap[station.routeId]
                        if((station.route?.route?.durationInMin ?: Int.MAX_VALUE) < ((currentStation?.route?.route?.durationInMin ?: Int.MAX_VALUE) as Nothing)) {
                            routeMap[station.routeId] = station
                        }
                    }
                    else {
                        routeMap[station.routeId] = station
                    }

                }

                val stations = mutableListOf<StationModel>()

                routeMap.forEach { (_, stationModel) ->
                    stations.add(stationModel)
                }

                viewModel.textViewBottomSheetRoutesFromToName.value = (viewModel.selectedFromLocation?.text?: viewModel.selectedFromDestination?.title)
                        .plus(" - ")
                        .plus(viewModel.selectedToLocation?.text?: viewModel.selectedToDestination?.title)


                viewModel.isReturningShuttleEdit = true

                viewModel.openBottomSheetRoutes.value = true

                viewModel.getStopsResponse.value = null
            }

        }

        viewModel.getAllNextRides()
        viewModel.getMyNextRides()


        viewModel.myNextRides.observe(viewLifecycleOwner) { myRides ->
           if(myRides.isEmpty()) {
                binding.cardViewRequestStation.visibility = View.VISIBLE
                binding.cardViewSearchWarning.visibility = View.VISIBLE
                binding.cardViewShuttle.visibility = View.GONE

               viewModel.myCampus()
               fillHomeLocation()

               fillDestinationNoRoute()

            }
           else {
               workgroupInstanceIdForVehicle = myRides.first().workgroupInstanceId

                binding.cardViewRequestStation.visibility = View.GONE
                binding.cardViewSearchWarning.visibility = View.GONE
                binding.cardViewShuttle.visibility = View.VISIBLE

                if(isVehicleLocationInit.not()) {
                    isVehicleLocationInit = true
                    viewModel.getVehicleLocation(workgroupInstanceIdForVehicle)
                }
            }

            binding.root.postDelayed({
                myNextRides = mutableListOf()

                myRides.forEach {  ride ->
                    val latestRide = if(myNextRides.isEmpty()) null else myNextRides.last()
                    if(latestRide != null
                            && latestRide.firstDepartureDate.getDateWithZeroHour() == ride.firstDepartureDate.getDateWithZeroHour()
                            && latestRide.fromType == ride.fromType
                            && latestRide.reserved.not()
                            && latestRide.workgroupStatus != WorkgroupStatus.PENDING_DEMAND) {
                        myNextRides.remove(latestRide)
                        viewModel.nextRides.value = myNextRides
                    }
                    else if(latestRide != null
                            && latestRide.firstDepartureDate.getDateWithZeroHour() == ride.firstDepartureDate.getDateWithZeroHour()
                            && latestRide.fromType == ride.fromType
                            && latestRide.workgroupStatus == WorkgroupStatus.PENDING_DEMAND
                            && ride.reserved.not()
                            && ride.workgroupStatus != WorkgroupStatus.PENDING_DEMAND
                    ) {
                        return@forEach
                    }
                    myNextRides.add(ride)
                    viewModel.nextRides.value = myNextRides
                }


                if(myNextRides.isNotEmpty()) {

                    val currentTime = System.currentTimeMillis()

                    var currentIndex = 0

                    for(i in myNextRides.indices) {
                        val ride = myNextRides[i]
                        if(ride.firstDepartureDate > currentTime) {
                            currentIndex = i
                            break
                        }
                    }

                    binding.imageViewShuttlePrev.alpha = if(currentIndex == 0) 0.5f else 1f
                    binding.imageViewShuttleNext.alpha = if(currentIndex < myNextRides.size-1)  1f else 0.5f

                    viewModel.currentMyRideIndex = currentIndex

                    val currentRide = myNextRides[currentIndex]

                    fillShuttleCardView(currentRide)
                }

            }, 50)
        }

        binding.cardViewRequestStation.setOnClickListener {
            val intent = Intent(requireActivity(), RouteSearchActivity::class.java)
            startActivity(intent)
        }

        binding.imageViewClose.setOnClickListener {
            binding.cardViewSearchWarning.visibility = View.GONE
        }

        binding.cardViewShuttle.setOnClickListener {
            viewModel.openReservationView.value = true
        }

        viewModel.getDestinations()

        viewModel.cancelDemandWorkgroupResponse.observe(viewLifecycleOwner) {
            if(it != null) {
                viewModel.cancelDemandWorkgroupResponse.value = null

                FlexigoInfoDialog.Builder(requireContext())
                        .setTitle(getString(R.string.title_success))
                        .setCancelable(false)
                        .setIconVisibility(false)
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                            viewModel.getMyNextRides()
                        }
                        .create()
                        .show()

            }

        }

        binding.buttonDriverNavigation.setOnClickListener {
            navigateToMapForDriver()
        }

        viewModel.searchRouteResponse.observe(viewLifecycleOwner) {

            if(viewModel.waitingForSearchResponse) {
                viewModel.waitingForSearchResponse = false
            }
            else {
                viewModel.setIsLoading(false)
                viewModel.setSearchListAdapter.value = true
            }

            val routeIdList = mutableListOf<Long>()

            (it as List<RouteModel>).forEach { route ->
                if (routeIdList.contains(route.id).not()) {
                    routeIdList.add(route.id)
                }
            }
        }

        viewModel.navigateToMapTrigger.observe(viewLifecycleOwner) {
            if(it != null) {
                viewModel.selectedStation?.let { station ->
                    navigateToMap(viewModel.myLocation?.latitude?:0.0, viewModel.myLocation?.longitude?:0.0, station.location.latitude, station.location.longitude)
                }

                viewModel.navigateToMapTrigger.value = null
            }
        }
    }


    private fun showAllMarkers(cardViewHeight: Int) {
        val builder = LatLngBounds.Builder()
        for (m in markerList)
            builder.include(m.position)

        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.30).toInt()

        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        googleMap!!.animateCamera(cu)

        googleMap!!.setPadding(0, cardViewHeight,0,0)


    }

    private fun navigateToMapForDriver(){

        val desAdd = "/" + viewModel.routesDetailsDriver.value!!.destination.location!!.latitude.toString() + "," + viewModel.routesDetailsDriver.value!!.destination.location!!.longitude

        var wayPoints = ""

        val isFirstLeg = cardCurrentRide?.workgroupDirection?.let { viewModel.isFirstLeg(it, cardCurrentRide?.fromType!!) } == true

        for (pointItem in viewModel.routesDetailsDriver.value!!.getRoutePath(isFirstLeg)!!.stations) {
            wayPoints = if (wayPoints != "")
                wayPoints + "/" + pointItem.location.latitude + "," + pointItem.location.longitude
            else
                "/" + pointItem.location.latitude + "," + pointItem.location.longitude
        }

        var link = "https://www.google.co.in/maps/dir$desAdd$wayPoints"
        if (cardCurrentRide != null && cardCurrentRide!!.fromType == FromToType.CAMPUS)
            link = "https://www.google.co.in/maps/dir$desAdd$wayPoints"
        else if (cardCurrentRide != null && cardCurrentRide!!.toType == FromToType.CAMPUS)
            link = "https://www.google.co.in/maps/dir$wayPoints$desAdd"

        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        try {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception){
            try {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e1: Exception) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(browserIntent)
                } catch (e2: java.lang.Exception) {
                    AppLogger.e(e, "NavigationAppNotFound")
                    Toast.makeText(requireContext(), R.string.Maps_No_Exist, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

        val currentTime = System.currentTimeMillis()

        val timeDiff = currentTime - lastVehicleUpdateTime

        if (timeDiff > timeIntervalToUpdateVehicle && workgroupInstanceIdForVehicle != null) {
            viewModel.getVehicleLocation(workgroupInstanceIdForVehicle)
        } else {
            vehicleRefreshHandler?.removeCallbacksAndMessages(null)
            vehicleRefreshHandler?.postDelayed(vehicleRefreshRunnable, timeIntervalToUpdateVehicle - timeDiff)
        }

        val timeDiffNextRides = currentTime - lastNextRidesUpdateTime

        if (timeDiffNextRides > timeIntervalToUpdateNextRides) {
            viewModel.getMyNextRidesForUpdate()
        } else {
            nextRidesRefreshHandler?.removeCallbacksAndMessages(null)
            nextRidesRefreshHandler?.postDelayed(myNextRidesRefreshRunnable, timeIntervalToUpdateNextRides - timeDiffNextRides)
        }

    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        vehicleRefreshHandler?.removeCallbacksAndMessages(null)
        nextRidesRefreshHandler?.removeCallbacksAndMessages(null)
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

    private fun fillUI(routeModel: RouteModel) {

        viewModel.routesDetailsDriver.value = routeModel

        googleMap?.clear()

        vehicleMarker = null
        val vehicleLocationResponse = viewModel.vehicleLocation.value
        if (vehicleLocationResponse != null) {
            fillVehicleLocation(vehicleLocationResponse.response)
        }

        fillHomeLocation()

        val isFirstLeg = cardCurrentRide?.workgroupDirection?.let { viewModel.isFirstLeg(it, cardCurrentRide?.fromType!!) } == true

        routeModel.getRoutePath(isFirstLeg)?.data?.let {
            fillPath(it)
        }
        routeModel.getRoutePath(isFirstLeg)?.stations?.let {
            stationsCount = it.size
            if (cardCurrentRide != null && cardCurrentRide!!.isDriver)
                viewModel.driverStationList.value = it
            fillStations(it)
        }


        fillDestination(routeModel)

//        if (cardCurrentRide != null && cardCurrentRide!!.isDriver) {
//            binding.buttonCallDriver.visibility = View.GONE
//            binding.buttonDriverNavigation.visibility = View.VISIBLE
//
//            viewModel.routeResponse.value.let {
//                if (it != null) {
//                   val x = if(it.persons == null) 0 else it.persons.size
//
//                    binding.textviewDriverStopsInfo.visibility = View.VISIBLE
//                    binding.textviewDriverStopsInfo.text = ", ".plus(stationsCount).plus(" ")
//                            .plus(context?.getString(R.string.stops)).plus(" ").plus(x)
//                            .plus(" ").plus(context?.getString(R.string.riders))
//                }
//            }
//        } else{
//            binding.buttonCallDriver.visibility = View.VISIBLE
//            binding.buttonDriverNavigation.visibility = View.GONE
//            binding.textviewDriverStopsInfo.visibility = View.GONE
//        }

        binding.textviewDriverStopsInfo.setOnClickListener {
            viewModel.openVanpoolDriverStations.value = true
        }

        binding.textViewCarInfo.text = routeModel.vehicle.carInfo()

        val driverName: String = routeModel.driver.name ?: ""
        val driverSurname: String = routeModel.driver.surname ?: ""

        if (driverName == "" && driverSurname == ""){
            binding.textViewDriverName.visibility = View.GONE
        } else
        {
            binding.textViewDriverName.visibility = View.VISIBLE
            binding.textViewDriverName.text = driverName.plus("  ").plus(driverSurname)
        }
        if (binding.cardViewShuttle.measuredHeight != 0)
            showAllMarkers(binding.cardViewShuttle.measuredHeight)

        binding.cardViewShuttle.viewTreeObserver.addOnGlobalLayoutListener {
            showAllMarkers(binding.cardViewShuttle.measuredHeight)
        }

    }

    private var stationsCount : Int = 0

    private fun fillPath(pointList: List<List<Double>>) {

        if (pointList.isNotEmpty()) {
            val options = PolylineOptions()
            val firstPoint = pointList[0]
            val lastPoint = pointList[pointList.lastIndex]
            if (lastPoint.size == 2) {
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

                if(viewModel.zoomStation.not()) {
                    val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 100)
                    try {
                        googleMap?.moveCamera(cu)
                    }
                    catch (e: Exception) {
                        Timber.e(e)
                    }
                }

            }

            googleMap?.addPolyline(options)

        }
    }

    private fun fillStations(stations: List<StationModel>) {

        viewModel.stations.value = stations

        for (station in stations) {

            val marker: Marker? = if (station.id == viewModel.selectedStation?.id) {
                googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(myStationIcon))
            } else {
                googleMap?.addMarker(MarkerOptions().position(LatLng(station.location.latitude, station.location.longitude)).icon(stationIcon))
            }
            marker?.tag = station

            if(station.id == viewModel.selectedStation?.id && viewModel.zoomStation) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(station.location.latitude, station.location.longitude), 14f))
            }

            if (marker != null) {
                markerList.add(marker)
            }

        }

    }

    private fun fillDestinationNoRoute() {

        viewModel.myCampus.observe(viewLifecycleOwner){
            if (it != null){

                val markerDestination: Marker? = googleMap?.addMarker(MarkerOptions().position(LatLng(it.location.latitude, it.location.longitude)).icon(workplaceIcon))

                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerDestination!!.position, 14f))

                if (markerDestination != null) {
                    markerList.add(markerDestination)
                }

                if (binding.cardViewRequestStation.measuredHeight != 0)
                    showAllMarkers(binding.cardViewRequestStation.measuredHeight)

                binding.cardViewRequestStation.viewTreeObserver.addOnGlobalLayoutListener {
                    showAllMarkers(binding.cardViewRequestStation.measuredHeight)
                }

            }

        }

    }

    private fun fillDestination(route: RouteModel?) {

        val defaultDestination = AppDataManager.instance.personnelInfo?.destination?.location!!

            val markerDestination: Marker? = googleMap?.addMarker(MarkerOptions().position(destinationLatLng ?: LatLng(defaultDestination.latitude, defaultDestination.longitude)).icon(workplaceIcon))
            markerDestination?.tag = route

            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerDestination!!.position, 14f))

            if (markerDestination != null) {
                markerList.add(markerDestination)
            }

    }

    private fun fillHomeLocation() {
        val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation

        homeLocation?.let {
            val location = LatLng(homeLocation.latitude, homeLocation.longitude)
            val markerHome = googleMap?.addMarker(MarkerOptions().position(location).icon(homeIcon))

            if (viewModel.myRouteDetails.value == null && viewModel.zoomStation.not()) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
            }
            if (markerHome != null) {
                markerList.add(markerHome)
            }
        }
    }

    private fun fillVehicleLocation(response: VehicleLocationModel) {

        val latitude: Double
        val longitude: Double
        val direction: Float
        try {
            latitude = response.LATITUDE?.toDouble() ?: 0.0
            longitude = response.LONGITUDE?.toDouble() ?: 0.0
            direction = response.DIRECTION?.toDouble()?.toFloat() ?: 0f
        } catch (e: java.lang.Exception) {
            vehicleMarker?.remove()
            vehicleMarker = null
            return
        }

        if (vehicleMarker == null) {
            vehicleMarker = googleMap?.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).icon(vehicleIcon).rotation(direction))
        } else {
            vehicleMarker?.position = LatLng(latitude, longitude)
            vehicleMarker?.rotation = direction
        }

        if (vehicleMarker != null) {
            markerList.add(vehicleMarker!!)
        }
    }

    private val vehicleRefreshRunnable = Runnable {

        if (viewModel.activeRide.value == true){
            if (workgroupInstanceIdForVehicle != null)
                viewModel.getVehicleLocation(workgroupInstanceIdForVehicle)
            viewModel.updateActiveRide()
        }

    }

    private val myNextRidesRefreshRunnable = Runnable {
            viewModel.getMyNextRidesForUpdate()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onLocationPermissionOk() {

        locationClient = FusedLocationClient(requireContext())

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            @SuppressLint("MissingPermission")
            override fun onLocationUpdated(location: Location) {
                AppDataManager.instance.currentLocation = location
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                googleMap?.isMyLocationEnabled = true

                binding.buttonCurrentLocation.visibility = View.VISIBLE

                viewModel.myLocation = location

                locationClient.stop()
//                if (viewModel.shouldFocusCurrentLocation) {
//                    val cu = CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude))
//                    googleMap?.animateCamera(cu)
//                    viewModel.shouldFocusCurrentLocation = false
//                }

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

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "ShuttleMainFragment"
        fun newInstance() = ShuttleMainFragment()

    }

    private var routeTime : String? = null
    private var routeName : String? = null

    private fun fillShuttleCardView(currentRide: ShuttleNextRide) {

        cardCurrentRide = currentRide
        viewModel.cardCurrentRide.value = currentRide
        viewModel.workgroupType.value = currentRide.workgroupType ?: "SHUTTLE"
        viewModel.activeRide.value = currentRide.activeRide
        viewModel.eta.value = currentRide.eta

        if (viewModel.workgroupType.value == "SHUTTLE")
            binding.imageViewVehicleIcon.setBackgroundResource(R.drawable.ic_shuttle_bottom_menu_shuttle)
        else
            binding.imageViewVehicleIcon.setBackgroundResource(R.drawable.ic_minivan)

        val date = currentRide.firstDepartureDate

        if (viewModel.stations.value != null){
            for (station in viewModel.stations.value!!){
                if (cardCurrentRide != null && cardCurrentRide!!.stationId == station.id) {
                    routeTime = station.expectedArrivalHour.convertHourMinutes(requireContext()) ?: ""
                    routeName = station.title ?: station.name
                }
            }
        }


        viewModel.isFromCampus = (currentRide.fromType == FromToType.CAMPUS || currentRide.fromType == FromToType.PERSONNEL_WORK_LOCATION) //outbound

        val timeValue = if (routeTime == null || routeTime.equals(""))
            date.convertToShuttleDateTime(requireContext())
        else
            routeTime

        if (viewModel.isFromCampus) {

                if (viewModel.eta.value != null)
                    binding.textViewShuttleDepartDateTimeInfo.text = getString(R.string.departure_from_campus, "<font color=#ff4663>${currentRide.eta}</font>")
                else
                    binding.textViewShuttleDepartDateTimeInfo.text = getString(R.string.departure_from_campus, timeValue)


        } else {
            if (currentRide.workgroupStatus == WorkgroupStatus.PENDING_DEMAND || currentRide.workgroupStatus == WorkgroupStatus.PENDING_PLANNING){

                if (viewModel.eta.value != null)
                    binding.textViewShuttleDepartDateTimeInfo.text = getString(R.string.arrival_at_campus).plus(" ").plus("<font color=#ff4663>${currentRide.eta}</font>")
                else
                    binding.textViewShuttleDepartDateTimeInfo.text = getString(R.string.arrival_at_campus).plus(" ").plus(timeValue)

            } else{

                if (viewModel.eta.value != null){
                    binding.textViewShuttleDepartDateTimeInfo.text = getString(R.string.departure_from_stop, "<font color=#ff4663>${currentRide.eta}</font>")
                } else
                    binding.textViewShuttleDepartDateTimeInfo.text = getString(R.string.departure_from_stop, timeValue)

            }


        }

        val dateFormat = if (getString(R.string.generic_language) == "tr"){
            date.convertToShuttleDateWithoutYear()
        } else {
            longToCalendar(date)!!.time.getCustomDateStringEN(withYear = false, withComma = false)
        }

        binding.textViewShuttleDepartDate.text = dateFormat
        binding.textViewRoute.text = if(currentRide.routeId == null) getString(R.string.pending_demand_assignment) else currentRide.routeName
        binding.textViewCarInfo.text = currentRide.vehiclePlate ?: ""

        binding.imageViewShuttlePrev.alpha = if(viewModel.currentMyRideIndex == 0)  0.5f else 1f
        binding.imageViewShuttleNext.alpha = if(viewModel.currentMyRideIndex < myNextRides.size -1)  1f else 0.5f

        binding.textViewShuttleState.visibility = View.GONE

        if (cardCurrentRide != null && cardCurrentRide!!.isDriver) {
            binding.buttonCallDriver.visibility = View.GONE
            binding.buttonDriverNavigation.visibility = View.VISIBLE

            viewModel.routeResponse.value.let {
                if (it != null) {
                    val x = if(it.persons == null) 0 else it.persons.size

                    binding.textviewDriverStopsInfo.visibility = View.VISIBLE
                    binding.textviewDriverStopsInfo.text = ", ".plus(stationsCount).plus(" ")
                            .plus(context?.getString(R.string.stops)).plus(" ").plus(x)
                            .plus(" ").plus(context?.getString(R.string.riders))
                }
            }
        } else{
            binding.buttonCallDriver.visibility = View.VISIBLE
            binding.buttonDriverNavigation.visibility = View.GONE
            binding.textviewDriverStopsInfo.visibility = View.GONE
        }

        if (currentRide.workgroupStatus == WorkgroupStatus.PENDING_PLANNING || currentRide.workgroupStatus == WorkgroupStatus.PENDING_DEMAND){

            binding.imageviewCircle.visibility = View.VISIBLE
            binding.imageviewCircle.setImageResource(R.drawable.circle_icon_marigold)
            binding.textViewRoute.text = getString(R.string.requested)
            binding.textViewRoute.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.textViewDriverName.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.textViewDriverName.text = getString(R.string.pending_planning)

        }

        binding.textViewShuttleDestinationInfo.text = getDestinationInfo()

        binding.imageViewEditRoute.setImageResource(if(currentRide.reserved || currentRide.routeId == null)  R.drawable.ic_close else R.drawable.ic_edit)

        currentRide.routeId?.let {routeId ->
            val routeIds = mutableSetOf<Long>()
            routeIds.add(routeId)

            viewModel.getRouteDetailsById(routeId)
        }

        if(currentRide.routeId == null) {
            googleMap?.clear()
            vehicleMarker = null
            val vehicleLocationResponse = viewModel.vehicleLocation.value
            if (vehicleLocationResponse != null) {
                fillVehicleLocation(vehicleLocationResponse.response)
            }
            fillHomeLocation()
            fillDestination(null)

            if (binding.cardViewShuttle.measuredHeight != 0)
                showAllMarkers(binding.cardViewShuttle.measuredHeight)

            binding.cardViewShuttle.viewTreeObserver.addOnGlobalLayoutListener {
                showAllMarkers(binding.cardViewShuttle.measuredHeight)
            }
        }


        if (viewModel.activeRide.value == true){

            binding.imageviewCircle.visibility = View.VISIBLE
            binding.imageviewCircle.setImageResource(R.drawable.circle_icon_green)

            binding.buttonComment.visibility = View.VISIBLE
            binding.buttonDriverNavigation.visibility = View.GONE
            binding.buttonCallDriver.visibility = View.GONE
            binding.cardViewShuttleEdit.visibility = View.GONE
            binding.textViewShuttleDepartDate.text = ""

            binding.textViewShuttleDepartDate.text = getString(R.string.now)
            binding.textViewShuttleDepartDateTime.visibility = View.VISIBLE

            binding.textViewShuttleDepartDateTimeInfo.text = "• ".plus(getString(R.string.eta_2).uppercase(Locale.getDefault()).plus(":"))
        }

        viewModel.eta.observe(viewLifecycleOwner){ eta ->
            if (eta != null){
                binding.textViewShuttleDepartDateTime.text = eta.convertHoursAndMinutes()

                lastNextRidesUpdateTime = System.currentTimeMillis()
                nextRidesRefreshHandler?.removeCallbacksAndMessages(null)
                nextRidesRefreshHandler?.postDelayed(myNextRidesRefreshRunnable, timeIntervalToUpdateNextRides)
            }
        }
    }

    private fun getDestinationInfo() : String{
        var destinationInfo = ""
        var destination : DestinationModel? = null
        var fromInfo = ""
        var toInfo = ""

        viewModel.destinations.value?.let { destinations ->
            destinations.forEachIndexed { _, destinationModel ->
                if (cardCurrentRide != null && (cardCurrentRide?.fromType == FromToType.CAMPUS || cardCurrentRide?.fromType == FromToType.PERSONNEL_WORK_LOCATION)){
                    if(destinationModel.id == cardCurrentRide!!.fromTerminalReferenceId) {
                        destination = destinationModel
                        fromInfo = getString(R.string.from).plus(" ").plus(destination?.title ?: "")
                        toInfo = getString(R.string.to).plus(" ").plus(routeName ?: getString(R.string.from_your_stop))

                        destinationInfo = fromInfo.plus(" - ").plus(toInfo)
                    }
                } else{
                    if(cardCurrentRide != null && destinationModel.id == cardCurrentRide!!.toTerminalReferenceId) {
                        destination = destinationModel
                        fromInfo = getString(R.string.from).plus(" ").plus(routeName ?: getString(R.string.from_your_stop))
                        toInfo = getString(R.string.to).plus(" ").plus(destination?.title ?: getString(R.string.from_your_campus))

                        destinationInfo = fromInfo.plus(" - ").plus(toInfo)
                    }
                }

            }
        }

        return destinationInfo
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

}