package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TransportStats(
    @SerializedName("bicycle")
    val bicycle: Distance,
    @SerializedName("car")
    val car: Distance,
    @SerializedName("id")
    val id: Int,
    @SerializedName("personnelId")
    val personnelId: Int,
    @SerializedName("publicBusCount")
    val publicBusCount: Int,
    @SerializedName("publicBusStationId")
    val publicBusStationId: Int,
    @SerializedName("publicDurationInMin")
    val publicDurationInMin: Int,
    @SerializedName("publicTransitDescription")
    val publicTransitDescription: String,
    @SerializedName("publicWalkingDurationToBusStop")
    val publicWalkingDurationToBusStop: Int,
    @SerializedName("publicWalkingDurationToDestination")
    val publicWalkingDurationToDestination: Int,
    @SerializedName("walking")
    val walking: Distance
): Serializable

data class Distance(
    @SerializedName("distanceInMeter")
    val distanceInMeter: Int,
    @SerializedName("durationInMin")
    val durationInMin: Int
): Serializable