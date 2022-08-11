package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CheckReservationResponse (
        @SerializedName("vehicleId")
        val vehicleId: Int,
        @SerializedName("plate")
        val plate: String,
        @SerializedName("status")
        val status: String,
        @SerializedName("doorLockType")
        val doorLockType: String,
        @SerializedName("calendarResourceId")
        val calendarResourceId: Int,
        @SerializedName("canbusOdometer")
        val canbusOdometer: Double?
): Serializable