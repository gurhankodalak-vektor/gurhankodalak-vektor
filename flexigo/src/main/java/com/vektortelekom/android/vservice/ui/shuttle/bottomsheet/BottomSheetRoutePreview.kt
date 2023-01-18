package com.vektortelekom.android.vservice.ui.shuttle.bottomsheet

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.databinding.BottomSheetRoutePreviewBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.route.adapter.RoutePreviewAdapter
import com.vektortelekom.android.vservice.ui.shuttle.map.ShuttleInfoWindowAdapter
import com.vektortelekom.android.vservice.utils.bitmapDescriptorFromVector
import timber.log.Timber
import javax.inject.Inject


class BottomSheetRoutePreview : BaseFragment<ShuttleViewModel>(), PermissionsUtils.LocationStateListener {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: BottomSheetRoutePreviewBinding

    private lateinit var locationClient: FusedLocationClient

    private var addressIcon: BitmapDescriptor? = null
    private var workplaceIcon: BitmapDescriptor? = null
    private var homeIcon: BitmapDescriptor? = null

    private var polyline: Polyline? = null
    private val polylineList: MutableList<Polyline> = ArrayList()
    private var stationMarkers: MutableList<Marker>? = ArrayList()

    private var googleMap: GoogleMap? = null
    private var destinationLatLng: LatLng? = null

    private var firstRouteId: Long? = null
    private var routePreviewAdapter : RoutePreviewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetRoutePreviewBinding>(inflater, R.layout.bottom_sheet_route_preview, container, false).apply {
            lifecycleOwner = this@BottomSheetRoutePreview
            viewModel = this@BottomSheetRoutePreview.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.getMapAsync {

            if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this))
                onLocationPermissionOk()
            else
                onLocationPermissionFailed()


            workplaceIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_workplace)
            addressIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_address)
            homeIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_home)

            googleMap = it
            googleMap?.setInfoWindowAdapter(ShuttleInfoWindowAdapter(requireActivity()))

            viewModel.zoomStation = false
            firstRouteId = viewModel.searchRoutesAdapterSetListTrigger.value?.first()?.id


            fillUI(viewModel.searchRoutesAdapterSetListTrigger.value!!)

            //default olarak gelen en yakın line renkli ve marker ile geliyor.
            polylineList.forEach{ polyItem ->
                if(polyItem.tag == firstRouteId) {
                    polyItem.color = Color.GREEN
                    addMarker(polyItem.points.component1())

                    showAllMarkers(binding.recyclerView.measuredHeight, polyItem)
                } else
                    polyItem.color = Color.BLACK
            }

            googleMap?.setOnPolylineClickListener { line ->
                for (tempPol in polylineList)
                    tempPol.color = Color.BLACK

//                if (stationMarkers != null) {
//                    for (marker_ in stationMarkers!!) {
//                        marker_.remove()
//                        stationMarkers = mutableListOf()
//                    }
//                }

                line.color = Color.GREEN
                line.zIndex = viewModel.searchRoutesAdapterSetListTrigger.value!!.size.toFloat()

                addMarker(line.points.component1())

                showAllMarkers(binding.recyclerView.measuredHeight, line)

                for (item in viewModel.searchRoutesAdapterSetListTrigger.value!!)
                    if (item.id == line.tag)
                        viewModel.searchedRoutePreview.value = item
                }

        }

        binding.mapView.onCreate(savedInstanceState)

        binding.textViewBottomSheetRoutesTitle.text = viewModel.textViewBottomSheetRoutesFromToName.value

        binding.imageViewList.setOnClickListener {
            openRoutesList()
        }

        binding.imageViewBottomSheetRoutesBack.setOnClickListener {
            openRoutesList()
        }

        binding.imageViewSort.setOnClickListener {
            viewModel.openNumberPicker.value = ShuttleViewModel.SelectType.RouteSorting
        }

        viewModel.searchedRoutePreview.value = viewModel.searchRoutesAdapterSetListTrigger.value?.first()

        viewModel.searchedRoutePreview.observe(viewLifecycleOwner) {
            if(it != null) {
                viewModel.searchRoutesAdapterSetListTrigger.value!!.forEachIndexed { index, list ->
                    if (list == it)
                        binding.recyclerView.scrollToPosition(index)
                }
            }
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val snapHelper: SnapHelper = PagerSnapHelper()
        binding.recyclerView.layoutManager = layoutManager
        snapHelper.attachToRecyclerView(binding.recyclerView)

        routePreviewAdapter = RoutePreviewAdapter(object: RoutePreviewAdapter.RoutePreviewListener {
            override fun seeStopsClick(route: RouteModel) {
                viewModel.routeSelectedForReservation.value  = route
            }

            override fun callDriverClick(phoneNumber: String?) {

                if (phoneNumber == null || phoneNumber == "") {
                    viewModel.navigator?.handleError(Exception(getString(R.string.error_empty_phone_number)))
                } else {
                    AppDialog.Builder(requireContext())
                            .setCloseButtonVisibility(false)
                            .setIconVisibility(false)
                            .setTitle(getString(R.string.call_2))
                            .setSubtitle(getString(R.string.will_call, phoneNumber))
                            .setOkButton(getString(R.string.Generic_Ok)) { d ->
                                d.dismiss()

                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(phoneNumber)))
                                startActivity(intent)

                            }
                            .setCancelButton(getString(R.string.cancel)) { d ->
                                d.dismiss()
                            }
                            .create().show()
                }

            }


        })

        binding.recyclerView.adapter = routePreviewAdapter
        binding.recyclerView.setHasFixedSize(true)
        routePreviewAdapter?.setList(viewModel.searchRoutesAdapterSetListTrigger.value!!)

        var position : Int? = null

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
           override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
               val layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

               if (position != layoutManager.findFirstVisibleItemPosition() && layoutManager.findFirstVisibleItemPosition() != -1) {
                   position = layoutManager.findFirstVisibleItemPosition()


                   polylineList.forEach{ polyItem ->
                       if(polyItem.tag == viewModel.searchRoutesAdapterSetListTrigger.value?.get(position!!)?.id) {
                           polyItem.color = Color.GREEN
//
//                           stationMarkers?.forEach{ marker ->
//                               marker.remove()
//                           }
//
//                           if (stationMarkers?.isNotEmpty() == true) stationMarkers?.clear()
                           addMarker(polyItem.points.component1())

                           showAllMarkers(binding.recyclerView.measuredHeight, polyItem)

                       } else
                           polyItem.color = Color.BLACK
                   }
               }
           }
       })


    }

    private fun showAllMarkers(recyclerViewHeight: Int, polyItem: Polyline) {
        val builder = LatLngBounds.Builder()
        builder.include(polyItem.points[0])
        builder.include(polyItem.points[polyItem.points.size - 1])

        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = if (polyItem.points.size > 380)
            (width * 0.40).toInt()
        else
            (width * 0.20).toInt()

        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        googleMap!!.animateCamera(cu)

        googleMap!!.setPadding(0, 0,0, recyclerViewHeight)

    }

    private fun addMarker(position: LatLng){

        val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation

        val  marker = if (viewModel.isLocationToHome.value == true)
            googleMap?.addMarker(MarkerOptions().position(LatLng(homeLocation!!.latitude, homeLocation.longitude)).icon(homeIcon))
        else
            googleMap?.addMarker(MarkerOptions().position(position).icon(addressIcon))

//        if (marker != null)
//            stationMarkers?.add(marker)

    }

    private fun fillUI(routeList : MutableList<RouteModel> ) {
        googleMap?.clear()

        val isFirstLeg = viewModel.workgroupTemplate?.fromType?.let { viewModel.workgroupTemplate?.direction?.let { it1 -> viewModel.isFirstLeg(it1, it) } } == true

        for (listItem in routeList){
            fillPath(isFirstLeg.let { listItem.getRoutePath(it) }!!.data, listItem.id)
        }

        fillDestination()

    }

    private fun fillPath(pointList: List<List<Double>>, id: Long) {

        if (pointList != null && pointList.isNotEmpty()) {
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

            polyline = googleMap?.addPolyline(options)
            polyline?.isClickable = true
            polyline?.tag = id
            polyline?.let { polylineList.add(it) }

        }
    }
    private fun fillDestination() {
        googleMap?.addMarker(MarkerOptions().position(destinationLatLng ?: LatLng(0.0, 0.0)).icon(workplaceIcon))
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

    private fun openRoutesList(){
        viewModel.isReturningShuttleEdit = true
        childFragmentManager.popBackStack()
        if (!viewModel.isComingSurvey)
            viewModel.openBottomSheetRoutes.value = true
        else
            viewModel.openRouteSelection.value = true
    }

    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "BottomSheetRoutePreview"

        fun newInstance() = BottomSheetRoutePreview()

    }

    override fun onLocationPermissionOk() {

        locationClient = FusedLocationClient(requireContext())

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            @SuppressLint("MissingPermission")
            override fun onLocationUpdated(location: Location) {

                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                googleMap?.isMyLocationEnabled = true

                viewModel.myLocation = location

                locationClient.stop()
                if (viewModel.shouldFocusCurrentLocation) {
                    val cu = CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude))
                    googleMap?.animateCamera(cu)

                    viewModel.shouldFocusCurrentLocation = false
                }

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

    override fun onLocationPermissionFailed() {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

}