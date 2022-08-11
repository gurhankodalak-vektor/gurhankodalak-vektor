package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ShuttleReservationRequest (
        @SerializedName("bookingDay")
        val bookingDay: String,
        @SerializedName("isIncoming")
        val isIncoming: Boolean,
        @SerializedName("isOutgoing")
        val isOutgoing: Boolean,
        @SerializedName("routeId")
        val routeId: Long,
        @SerializedName("stationId")
        val stationId: Long
) : Serializable