package com.vektortelekom.android.vservice.ui.poi.gasstation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.service.LocationHelper
import com.vektor.ktx.ui.fragment.VMapFragment
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.ktx.utils.logger.AppLogger
import com.vektor.ktx.utils.map.VClusterItem
import com.vektor.ktx.utils.map.interfaces.VClusterItemClickListener
import com.vektor.vshare_api_ktx.model.PoiRequest
import com.vektor.vshare_api_ktx.model.PoiResponse
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.ParkModel
import com.vektortelekom.android.vservice.databinding.GasStationActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.poi.gasstation.adapter.ParkClusterItem
import com.vektortelekom.android.vservice.ui.poi.gasstation.adapter.ParkClusterRenderer
import com.vektortelekom.android.vservice.ui.poi.gasstation.adapter.ParkInfoWindowAdapter
import java.util.ArrayList
import javax.inject.Inject

class GasStationActivity : BaseActivity<GasStationViewModel>(), BaseNavigator, GoogleMap.OnCameraIdleListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: GasStationViewModel

    private val locationLock = Any()
    @Volatile
    private var myLocation: Location? = null
    private lateinit var locationClient: FusedLocationClient

    private lateinit var poiIcon: BitmapDescriptor

    var isGas = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<GasStationActivityBinding>(this, R.layout.gas_station_activity).apply {
            lifecycleOwner = this@GasStationActivity
            viewModel = this@GasStationActivity.viewModel
        }
        viewModel.navigator = this

        isGas = intent != null && intent.getStringExtra("type") == "gas"

        binding.tvToolbarTitle.text = if(isGas) getString(R.string.nearest_gas_station) else getString(R.string.nearest_stations)

        binding.back.setOnClickListener {
            finish()
        }

        locationClient = FusedLocationClient(this)

        val mapFragment = (supportFragmentManager.findFragmentById(R.id.map) as VMapFragment)
        mapFragment.infoWindowAdapter = ParkInfoWindowAdapter(this)
        mapAssetsReady(mapFragment)

        poiIcon = if(isGas) {
            bitmapDescriptorFromVector(this, R.drawable.ic_marker_gas_station)
        }
        else {
            bitmapDescriptorFromVector(this, R.drawable.ic_station_marker)
        }

        viewModel.poiList.observe(this) { list ->
            setupParkListOnMap(list, true)
        }

        viewModel.stations.observe(this) { list ->
            setupStationsOnMap(list, true)
        }

    }

    override fun getViewModel(): GasStationViewModel {
        viewModel = ViewModelProvider(this, factory)[GasStationViewModel::class.java]
        return viewModel
    }

    private fun mapAssetsReady(mapFragment: VMapFragment) {
        mapFragment.addAssetSetChangedObserver {
            AppLogger.i("assetsReady called")
            startLocationUpdate()
        }

        mapFragment.addClusterItemListener(object : VClusterItemClickListener {
            override fun handleEvent(item: VClusterItem?) {

                if (myLocation != null && item != null && item.position != null)
                    navigateToMap(myLocation!!.latitude, myLocation!!.longitude, item.position.latitude, item.position.longitude)

            }

            override fun handleMapClickEvent() {
                AppLogger.d("handleMapClickEvent")
            }
        })

//        if (mapFragment.mMapView != null) {
//
//            val screenSize = windowManager.defaultDisplay
//            var size = Point()
//            screenSize.getSize(size)
//
//            val view1 = mapFragment.mMapView.findViewById<View>(Integer.parseInt("1"))
//            // Get the button view
//            val locationButton: View = (view1.parent as View).findViewById(Integer.parseInt("2"))
//            // and next place it, on bottom right (as Google Maps app)
//            val layoutParams: RelativeLayout.LayoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
////            // position on top right
//            layoutParams.setMargins(0, 60, 60, 0)
//        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        } ?: throw RuntimeException("Bitmap was not generated")
    }

    private fun getVMapFragment(): VMapFragment {
        return (supportFragmentManager.findFragmentById(R.id.map)  as VMapFragment)
    }

    private fun startLocationUpdate() {
        if (PermissionsUtils.isLocationPermissionOk(this)) {
            val mapFragment = getVMapFragment()
            if (mapFragment.mMap != null) {
                AppLogger.d("startLocationUpdate called.")
                getLocationInternal()
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun getLocationInternal() {
        val mapFragment = getVMapFragment()
        if (mapFragment.mMap != null && LocationHelper.isLocationGranted(this)) {
            mapFragment.mMap.isMyLocationEnabled = true
        }

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            override fun onLocationUpdated(location: Location) {
                AppDataManager.instance.currentLocation = location
                synchronized(locationLock) {
                    myLocation = location
                    AppLogger.d("getLocation. lat: " + myLocation?.getLatitude() + " lng: " + myLocation?.getLongitude())
                    AppDataManager.instance.currentLocation = myLocation
                    mapFragment.mMap.isMyLocationEnabled = true
                    mapFragment.mMap.uiSettings.isMyLocationButtonEnabled = true
                    try {
                        val map = mapFragment.mMap
                        if (map != null) {
                            val newCamPos = CameraPosition(LatLng(location.latitude, location.longitude),
                                    12f,
                                    map.cameraPosition.tilt,
                                    map.cameraPosition.bearing)

                            map.moveCamera(CameraUpdateFactory.newCameraPosition(newCamPos))
                            map.setOnCameraIdleListener(this@GasStationActivity)
                        }
                    } catch (e: Exception) {
                        AppLogger.e(e, "onLocationUpdated failed")
                    }

                    if (this@GasStationActivity.isFinishing || this@GasStationActivity.isDestroyed) {
                        locationClient.stop()
                        return
                    }

                    if (location.accuracy < 99.0f) {
                        locationClient.stop()
                        if(isGas) {
                            getPoiList()
                        }
                        else {
                            getStations()
                        }
                    }
                }
            }

            override fun onLocationFailed(message: String) {
                AppLogger.w("onLocationFailed: %s", message)

                if (this@GasStationActivity.isFinishing || this@GasStationActivity.isDestroyed)
                    return

                when (message) {
                    FusedLocationClient.ERROR_LOCATION_DISABLED -> locationClient.showLocationSettingsDialog()
                    FusedLocationClient.ERROR_LOCATION_MODE -> {
                        locationClient.showLocationSettingsDialog()
                    }
                    FusedLocationClient.ERROR_TIMEOUT_OCCURRED -> {
                        handleError(RuntimeException(getString(R.string.location_timeout)))
                    }
                }
            }
        })
    }

    override fun onCameraIdle() {

    }

    fun getPoiList() {
        val poiRequest = PoiRequest()
        poiRequest.latitude = myLocation?.latitude
        poiRequest.longitude = myLocation?.longitude
        poiRequest.type = "GAS"

        viewModel.getPoiList(poiRequest)
    }

    fun getStations() {
        viewModel.getStations()
    }

    private fun setupParkListOnMap(poiList: List<PoiResponse>?, showOnMap: Boolean) {
        val mapFragment = (supportFragmentManager.findFragmentById(R.id.map) as VMapFragment)
        if (mapFragment.mMap == null)
            return

        val manager = mapFragment.clusterManager
        if (manager != null) {
            mapFragment.setClusterRenderer(ParkClusterRenderer(this, mapFragment.mMap, manager))
            manager.clearItems()
        }

        if (showOnMap && poiList != null && poiList.isNotEmpty()) {

            val vClusterItems = ArrayList<VClusterItem>()
            for (poi in poiList) {
                if (poi.latitude != null && poi.longitude != null) {
                    val item = ParkClusterItem(poi.latitude!!, poi.longitude!!, "", "", "", poiIcon)
                    item.tag = poi
                    vClusterItems.add(item)
                }
            }

            if (manager != null) {
                mapFragment.triggerClusterObservers(false)
                mapFragment.addItemsOnMap(vClusterItems)
                manager.cluster()
            }
        }
    }

    private fun setupStationsOnMap(poiList: List<ParkModel>?, showOnMap: Boolean) {
        val mapFragment = (supportFragmentManager.findFragmentById(R.id.map) as VMapFragment)
        if (mapFragment.mMap == null)
            return

        val manager = mapFragment.clusterManager
        if (manager != null) {
            mapFragment.setClusterRenderer(ParkClusterRenderer(this, mapFragment.mMap, manager))
            manager.clearItems()
        }

        if (showOnMap && poiList != null && poiList.isNotEmpty()) {

            val vClusterItems = ArrayList<VClusterItem>()
            for (poi in poiList) {
                if (poi.latitude != null && poi.longitude != null) {
                    val item = ParkClusterItem(poi.latitude, poi.longitude, "", "", "", poiIcon)
                    item.tag = poi
                    vClusterItems.add(item)
                }
            }

            if (manager != null) {
                mapFragment.triggerClusterObservers(false)
                mapFragment.addItemsOnMap(vClusterItems)
                manager.cluster()
            }
        }
    }

}