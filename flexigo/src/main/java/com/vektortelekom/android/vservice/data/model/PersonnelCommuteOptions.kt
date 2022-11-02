package com.vektortelekom.android.vservice.data.model

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.vektortelekom.android.vservice.R
import java.io.Serializable

data class PersonnelCommuteOptions(
    @SerializedName("bestCommuteMode")
    val bestCommuteMode: String?,
    @SerializedName("bestDistanceInKm")
    val bestDistanceInKm: Double?,
    @SerializedName("bestDurationInMin")
    val bestDurationInMin: Int?,
    @SerializedName("bestEmission")
    val bestEmission: Double?,
    @SerializedName("bestMonthlyCost")
    val bestMonthlyCost: Double?,
    @SerializedName("carDistanceInMeter")
    val carDistanceInMeter: Int?,
    @SerializedName("carDurationInMin")
    val carDurationInMin: Int?,
    @SerializedName("commuteMode")
    val commuteMode: String?,
    @SerializedName("commuteModeCost")
    val commuteModeCost: Commute?,
    @SerializedName("commuteModeEmission")
    val commuteModeEmission: Commute?,
    @SerializedName("distanceInKm")
    val distanceInKm: Double?,
    @SerializedName("durationInMin")
    val durationInMin: Int?,
    @SerializedName("emission")
    val emission: Double?,
    @SerializedName("monthlyCost")
    val monthlyCost: Double?,
    @SerializedName("personnelId")
    val personnelId: Int?,
    @SerializedName("personnelName")
    val personnelName: String?,
    @SerializedName("personnelSurname")
    val personnelSurname: String?,
    @SerializedName("publicDurationInMin")
    val publicDurationInMin: Int?,
    @SerializedName("shuttleDurationInMin")
    val shuttleDurationInMin: Int?,
    @SerializedName("walkingDistanceToStop")
    val walkingDistanceToStop: Int?,
    @SerializedName("walkingDurationToStop")
    val walkingDurationToStop: Int?
): Serializable

data class Commute(
    val DRIVING: Double,
    val SHUTTLE: Double,
    val TRANSIT: Double
)
