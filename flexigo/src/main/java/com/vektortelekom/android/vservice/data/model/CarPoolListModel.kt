package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CarPoolListModel (
        @SerializedName("id")
        val id: Long,
        @SerializedName("name")
        val name: String,
        @SerializedName("surname")
        val surname: String,
        @SerializedName("department")
        val department: String?,
        @SerializedName("arrivalHour")
        val arrivalHour: Int?,
        @SerializedName("departureHour")
        val departureHour: Int?,
        @SerializedName("distanceInMeter")
        val distanceInMeter: Double?,
        @SerializedName("durationInMin")
        val durationInMin: Double?,
        @SerializedName("phoneNumber")
        val phoneNumber: String?,
        @SerializedName("homeLocation")
        val homeLocation: LocationModel?,
): Serializable