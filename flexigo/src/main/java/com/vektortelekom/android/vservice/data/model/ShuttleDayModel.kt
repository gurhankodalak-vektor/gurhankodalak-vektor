package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ShuttleDayModel (
        @SerializedName("shuttleDay")
        val shuttleDay : String,
        @SerializedName("isIncoming")
        var isIncoming : Boolean?,
        @SerializedName("isOutgoing")
        var isOutgoing : Boolean?,
        @SerializedName("isMesai")
        var isMesai : Boolean?,
        @SerializedName("bookedIncomingRoute")
        val bookedIncomingRoute: ShuttleDayReservation?,
        @SerializedName("bookedIncomingStation")
        val bookedIncomingStation: ShuttleDayReservation?,
        @SerializedName("bookedOutgoingRoute")
        val bookedOutgoingRoute: ShuttleDayReservation?,
        @SerializedName("bookedOutgoingStation")
        val bookedOutgoingStation: ShuttleDayReservation?

) : Serializable

data class ShuttleDayReservation (
        @SerializedName("id")
        val id: Int?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("driver")
        val driver: DriverInfo?,
        @SerializedName("vehicle")
        val vehicle: VehicleInfo?
): Serializable

data class DriverInfo(
        @SerializedName("phoneNumber")
        val phoneNumber: String?
)

data class VehicleInfo(
        @SerializedName("plateId")
        val plateId: String?
)