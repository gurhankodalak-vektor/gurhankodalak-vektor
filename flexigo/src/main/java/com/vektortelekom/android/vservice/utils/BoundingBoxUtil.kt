package com.vektortelekom.android.vservice.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.RectangularBounds
import kotlin.math.cos

class BoundingBoxUtil {

    fun getBoundingBox(loc: Location, distance: Double): RectangularBounds {
        val latRadian = (loc.latitude * Math.PI) / 180
        val equatorCircumference: Double = 6371000.0
        val polarCircumference: Double = 6356800.0
        val cosinus = cos((latRadian * Math.PI) / 180)
        val mPerDegLong = (360 / polarCircumference)

        val mPerDegLat = 360 / (cosinus * equatorCircumference)
        val degDiffLong = distance * mPerDegLong
        val degDiffLat = distance * mPerDegLat

        val xxNorthLat = loc.latitude + degDiffLat
        val xxSouthLat = loc.latitude - degDiffLat
        val xxEastLong = loc.longitude + degDiffLong
        val xxWestLong = loc.longitude - degDiffLong
        return RectangularBounds.newInstance(
                LatLng(xxSouthLat, xxWestLong),
                LatLng(xxNorthLat, xxEastLong)
        )
    }
}