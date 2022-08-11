package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LstTrackPosition(
        @SerializedName("latitude")
        val latitude: Double,
        @SerializedName("longitude")
        val longitude: Double
): Serializable

data class Path(
        @SerializedName("distanceInKm")
        val distanceInKm: Double,
        @SerializedName("durationInMin")
        val durationInMin: Int,
        @SerializedName("lstRouteStopIds")
        val lstRouteStopIds: MutableList<Int>,
        @SerializedName("lstTrackPositions")
        val lstTrackPositions: MutableList<LstTrackPosition>
): Serializable