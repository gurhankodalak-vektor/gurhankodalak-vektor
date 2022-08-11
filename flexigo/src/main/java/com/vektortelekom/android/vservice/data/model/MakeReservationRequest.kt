package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MakeReservationRequest (
        @SerializedName("parkId")
        val parkId: Int,
        @SerializedName("toLocation")
        val toLocation: TaxiLocationModel,
        @SerializedName("startTime")
        val startTime: String,
        @SerializedName("endTime")
        val endTime: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("vehicleCatalogId")
        val vehicleCatalogId: Int
): Serializable