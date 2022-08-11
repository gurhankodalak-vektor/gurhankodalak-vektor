package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ShuttleReservationCancelRequest (
        @SerializedName("bookingDay")
        val bookingDay: String,
        @SerializedName("isIncoming")
        val isIncoming: Boolean,
        @SerializedName("isOutgoing")
        val isOutgoing: Boolean,
        @SerializedName("routeId")
        val routeId: Int,
        @SerializedName("stationId")
        val stationId: Int
): Serializable