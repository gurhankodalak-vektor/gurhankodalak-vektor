package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class PoolCarIntercityFinishRequest(
        @SerializedName("endOdometer")
        val endOdometer: Double,
        @SerializedName("photoUuid")
        val photoUuid: String,
        @SerializedName("endLocation")
        val endLocation: TaxiLocationModel
)