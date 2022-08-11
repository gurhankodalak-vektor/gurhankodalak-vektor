package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FlexirideCreateRequestModel (
        @SerializedName("requestTime")
        val requestTime: String,
        @SerializedName("fromLocation")
        val fromLocation: TaxiLocationModel,
        @SerializedName("toLocation")
        val toLocation: TaxiLocationModel,
        @SerializedName("passengerCount")
        val passengerCount: Int,
        @SerializedName("requiredChildSeats")
        val requiredChildSeats: Int
) : Serializable