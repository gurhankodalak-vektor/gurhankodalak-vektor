package com.vektortelekom.android.vservice.ui.route.search

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.DestinationModel
import com.vektortelekom.android.vservice.data.model.FromToType
import com.vektortelekom.android.vservice.databinding.RouteSearchFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class RouteSearchFragment : BaseFragment<RouteSearchViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RouteSearchViewModel

    lateinit var binding: RouteSearchFragmentBinding

    private var googleMap: GoogleMap? = null

    private var workplaceIcon: BitmapDescriptor? = null
    private var homeIcon: BitmapDescriptor? = null
    private var toLocationIcon: BitmapDescriptor? = null

    private lateinit var locationClient: FusedLocationClient

    private var toLocationMarker: Marker? = null
    private var directionMarker: Marker? = null

    private var destinationInfo = ""
    var destination : DestinationModel? = null
    private var hasInitializedRootView = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate<RouteSearchFragmentBinding>(
                inflater,
                R.layout.route_search_fragment,
                container,
                false
            ).apply {
                lifecycleOwner = this@RouteSearchFragment
                viewModel = this@RouteSearchFragment.viewModel
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!hasInitializedRootView) {
            hasInitializedRootView = true


            binding.mapView.onCreate(savedInstanceState)

            (requireActivity() as BaseActivity<*>).showPd()

            binding.mapView.getMapAsync { map ->
                googleMap = map
                googleMap!!.uiSettings.isZoomControlsEnabled = true

                if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
                    onLocationPermissionOk()
                }
                else {
                    onLocationPermissionFailed()
                }

                googleMap?.setInfoWindowAdapter(ShuttleInfoWindowAdapter(requireActivity()))

                workplaceIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_workplace)
                homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)
                toLocationIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_route_to_yellow)


                viewModel.toLocation.value = AppDataManager.instance.personnelInfo?.homeLocation

                val location = Location("")
                location.latitude = viewModel.toLocation.value?.latitude ?: 0.0
                location.longitude = viewModel.toLocation.value?.longitude ?: 0.0

                viewModel.selectedToLocation = ShuttleViewModel.FromToLocation(
                    location = location,
                    text = getString(R.string.home_address),
                    destinationId = null
                )

                viewModel.toLabelText.value = viewModel.selectedToLocation!!.text

                if (viewModel.toLocation.value == null) {
                    viewModel.toLabelText.value = getString(R.string.select_address)
                    binding.textviewBottomSheetToValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
                } else{
                    viewModel.toLabelText.value = viewModel.selectedToLocation?.text
                }

                viewModel.fromIcon.value = R.drawable.ic_from

                if(viewModel.isLocationToHome.value == true)
                    viewModel.toIcon.value = R.drawable.ic_route_to
                else
                    viewModel.toIcon.value = R.drawable.ic_route_to_yellow

            }

            binding.mapView.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels - 200f.dpToPx(requireContext())

            viewModel.getAllNextRides()

            viewModel.destinations.observe(viewLifecycleOwner){
                if (it != null ){
                    if (viewModel.currentWorkgroup.value == null)
                        getCurrentWorkgroup()
                    getDestinationInfo()
                }
            }

        }


        viewModel.isFromEditPage.observe(viewLifecycleOwner){
            if (it != null && it ){
                if(viewModel.isLocationToHome.value == true)
                    viewModel.toIcon.value = R.drawable.ic_route_to
                else
                    viewModel.toIcon.value = R.drawable.ic_route_to_yellow

                googleMap?.let { it1 -> drawArcPolyline(it1, LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude), LatLng(viewModel.fromLocation.value!!.latitude, viewModel.fromLocation.value!!.longitude)) }
            }
        }

        binding.imagebuttonToEdit.setOnClickListener{
            if (viewModel.isFromChanged.value == true)
                viewModel.openNumberPicker.value = RouteSearchViewModel.SelectType.CampusFrom
            else
                viewModel.openBottomSheetSearchLocation.value = true
        }

        binding.imagebuttonFromEdit.setOnClickListener{
            if (viewModel.isFromChanged.value == false)
                viewModel.openNumberPicker.value = RouteSearchViewModel.SelectType.CampusFrom
            else
                viewModel.openBottomSheetSearchLocation.value = true

        }

        binding.imageviewBack.setOnClickListener {
            activity?.finish()
        }

        binding.buttonSelect.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_routeSearchFragment_to_routeSearchTimeSelectionFragment)
        }

        binding.imageviewReplace.setOnClickListener {
            val tempToLabel = viewModel.toLabelText.value
            val tempFromLabel = viewModel.fromLabelText.value

            val tempToLocation = viewModel.toLocation.value
            val tempFromLocation = viewModel.fromLocation.value

            viewModel.toLocation.value = tempFromLocation
            viewModel.fromLocation.value = tempToLocation

            viewModel.toLabelText.value = tempFromLabel
            viewModel.fromLabelText.value = tempToLabel

            val tempToIcon = viewModel.toIcon.value
            val tempFromIcon = viewModel.fromIcon.value

            viewModel.toIcon.value = tempFromIcon
            viewModel.fromIcon.value = tempToIcon

            val tempIsFromChanged = viewModel.isFromChanged.value
            viewModel.isFromChanged.value = tempIsFromChanged != true

            val tempRotation = directionMarker?.rotation
            directionMarker?.rotation = tempRotation?.plus(180)!!

        }

    }

    private fun getCurrentWorkgroup(){
        for (workgroup in viewModel.allWorkgroup.value!!){
            val date1: Date? = longToCalendar(Calendar.getInstance().time.time)?.time!!.convertForTimeCompare()
            val date2: Date? = longToCalendar(workgroup.firstDepartureDate)?.time!!.convertForTimeCompare()

            if (workgroup.firstDepartureDate != null && (date1?.compareTo(date2) == 0)){
                if (workgroup.fromType == FromToType.PERSONNEL_WORK_LOCATION || workgroup.fromType == FromToType.CAMPUS){
                    if (viewModel.selectedFromDestination != null && workgroup.fromTerminalReferenceId == viewModel.selectedFromDestination?.id){
                        viewModel.currentWorkgroup.value = workgroup
                    } else if (workgroup.fromTerminalReferenceId == AppDataManager.instance.personnelInfo?.destination?.id){
                        viewModel.currentWorkgroup.value = workgroup
                    }
                } else if (workgroup.toType == FromToType.PERSONNEL_WORK_LOCATION || workgroup.toType == FromToType.CAMPUS){
                    if (viewModel.selectedFromDestination != null && workgroup.toTerminalReferenceId == viewModel.selectedFromDestination?.id){
                        viewModel.currentWorkgroup.value = workgroup
                    } else if (workgroup.toTerminalReferenceId == AppDataManager.instance.personnelInfo?.destination?.id){
                        viewModel.currentWorkgroup.value = workgroup
                    }
                }
            }
        }
    }

    private fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Double {
        val lat1 = latLng1.latitude
        val long1 = latLng1.longitude
        val lat2 = latLng2.latitude
        val long2 = latLng2.longitude
        val dLon = long2 - long1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - (sin(lat1) * cos(lat2) * cos(dLon))
        var brng = atan2(y, x)
        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360

        return brng
    }

    private fun drawArcPolyline(googleMap: GoogleMap, latLng1: LatLng, latLng2: LatLng) {
        googleMap.clear()
        toLocationMarker?.remove()

        toLocationMarker = if (viewModel.isLocationToHome.value == true)
            googleMap.addMarker(MarkerOptions().position(latLng1).icon(homeIcon))
        else
            googleMap.addMarker(MarkerOptions().position(latLng1).icon(toLocationIcon))

        val destinationMarker = googleMap.addMarker(MarkerOptions().position(latLng2).icon(workplaceIcon))

        if (destinationMarker != null) {
            destinationMarker.tag = destination
        }
        if (toLocationMarker != null) {
            toLocationMarker!!.tag = viewModel.selectedToLocation!!.text
        }

        val builder = LatLngBounds.Builder()

        builder.include(toLocationMarker!!.position)
        builder.include(destinationMarker!!.position)

        val bounds = builder.build()

        try {
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, 150)
            googleMap.moveCamera(cu)
            googleMap.animateCamera(cu)
        }
        catch (e: Exception) {

        }

        val k = 0.5 //curve radius
        var h = SphericalUtil.computeHeading(latLng1, latLng2)
        var d = 0.0
        val p: LatLng?

        if (h < 0) {
            d = SphericalUtil.computeDistanceBetween(latLng2, latLng1)
            h = SphericalUtil.computeHeading(latLng2, latLng1)
            //Midpoint position
            p = SphericalUtil.computeOffset(latLng2, d * 0.5, h)
        } else {
            d = SphericalUtil.computeDistanceBetween(latLng1, latLng2)
            p = SphericalUtil.computeOffset(latLng1, d * 0.5, h)
        }

        val x = (1 - k * k) * d * 0.5 / (2 * k)
        val r = (1 + k * k) * d * 0.5 / (2 * k)

        val c = SphericalUtil.computeOffset(p, x, h + 90.0)

        //Calculate heading between circle center and two points
        val h1 = SphericalUtil.computeHeading(c, latLng1)
        val h2 = SphericalUtil.computeHeading(c, latLng2)

        val numberOfPoints = 1000
        val step = (h2 - h1) / numberOfPoints

        val polygon = PolygonOptions()
        val temp = arrayListOf<LatLng>()

        for (i in 0 until numberOfPoints) {
            val latlng = SphericalUtil.computeOffset(c, r, h1 + i * step)
            polygon.add(latlng)
            temp.add(latlng)
        }

        for (i in (temp.size - 1) downTo 1) {
            polygon.add(temp[i])
        }

        polygon.strokeColor(Color.BLACK)
        polygon.strokeWidth(10f)
        googleMap.addPolygon(polygon)

        directionMarker = googleMap.addMarker(MarkerOptions().position(temp[numberOfPoints/2]).anchor(0.5f,0.5f)
            .rotation((90 - bearingBetweenLocations(latLng2, latLng1)).toFloat())
            .flat(true)
            .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_back)))

        temp.clear()

        (requireActivity() as BaseActivity<*>).dismissPd()
    }


    private fun fillDestination(destination : DestinationModel) {
        viewModel.fromLocation.value = destination.location
        googleMap?.let { drawArcPolyline(it, LatLng(viewModel.toLocation.value!!.latitude, viewModel.toLocation.value!!.longitude), LatLng(viewModel.fromLocation.value!!.latitude, viewModel.fromLocation.value!!.longitude)) }
    }

    private fun getDestinationInfo() : String{

        viewModel.currentWorkgroup.value?.workgroupInstanceId?.let {
            viewModel.getWorkgroupInformation(
                it
            )
        }

        viewModel.destinations.value?.let { destinations ->
            destinations.forEachIndexed { _, destinationModel ->
                if (viewModel.currentWorkgroup.value != null && (viewModel.currentWorkgroup.value?.fromType == FromToType.CAMPUS || viewModel.currentWorkgroup.value?.fromType == FromToType.PERSONNEL_WORK_LOCATION)){
                    if(destinationModel.id == viewModel.currentWorkgroup.value?.fromTerminalReferenceId) {
                        destination = destinationModel
                        destinationInfo = destination?.title ?: ""
                    }
                } else{
                    if(viewModel.currentWorkgroup.value != null && destinationModel.id == viewModel.currentWorkgroup.value?.toTerminalReferenceId) {
                        destination = destinationModel
                        destinationInfo = destination?.title ?: ""
                    }
                }

            }
        }

        if (destination == null){
            destination = viewModel.destinations.value?.first()
            destinationInfo = destination?.title ?: ""
        }

        viewModel.destinationId = destination!!.id

        if (viewModel.currentWorkgroup.value != null)
            viewModel.fromToType = viewModel.currentWorkgroup.value?.fromType
        else {
            viewModel.fromToType = FromToType.CAMPUS
        }

        viewModel.fromLabelText.value = destinationInfo
        destination?.let { fillDestination(it) }

        return destinationInfo
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
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

    override fun getViewModel(): RouteSearchViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RouteSearchViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "RouteSearchFragment"
        fun newInstance() = RouteSearchFragment()

    }

    override fun onLocationPermissionFailed() {
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
}