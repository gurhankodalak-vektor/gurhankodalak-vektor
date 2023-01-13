package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WalkingPath(
    @SerializedName("distanceInMeter")
    val distanceInMeter: Double,
    @SerializedName("durationInMin")
    val durationInMin: Double,
    @SerializedName("pathLocations")
    val pathLocations: List<List<Double>>,
    @SerializedName("startingLocation")
    val startingLocation: StartingLocation
): Serializable