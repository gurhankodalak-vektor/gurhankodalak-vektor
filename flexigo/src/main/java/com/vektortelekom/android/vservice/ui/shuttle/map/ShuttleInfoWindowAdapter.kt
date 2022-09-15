package com.vektortelekom.android.vservice.ui.shuttle.map

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DestinationModel
import com.vektortelekom.android.vservice.data.model.LocationModel
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.data.model.StationModel

class ShuttleInfoWindowAdapter(activity: Activity) : GoogleMap.InfoWindowAdapter {

    private var contentsView : View = activity.layoutInflater.inflate(R.layout.shuttle_station_info_window, activity.window.decorView.rootView as ViewGroup, false)

    override fun getInfoContents(marker: Marker): View? {

        return when(marker.tag) {
            is StationModel -> {
                val stationModel = marker.tag as StationModel
                val textViewTitle= contentsView.findViewById<TextView>(R.id.title)
                textViewTitle.text = stationModel.title
                contentsView
            }
            is RouteModel -> {
                val routeModel = marker.tag as RouteModel
                val textViewTitle= contentsView.findViewById<TextView>(R.id.title)
                textViewTitle.text = routeModel.destination.name
                contentsView
            }
            is DestinationModel -> {
                val destinationModel = marker.tag as DestinationModel
                val textViewTitle= contentsView.findViewById<TextView>(R.id.title)
                textViewTitle.text = destinationModel.title
                contentsView
            }
            is LocationModel -> {
                val locationModel = marker.tag as LocationModel
                val textViewTitle= contentsView.findViewById<TextView>(R.id.title)
                textViewTitle.text = locationModel.address
                contentsView
            }
            is String -> {
                val text = marker.tag as String
                val textViewTitle= contentsView.findViewById<TextView>(R.id.title)
                textViewTitle.text = text
                contentsView
            }
            else -> {
                null
            }
        }

    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }

}