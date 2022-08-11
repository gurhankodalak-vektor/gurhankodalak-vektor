package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class PoolCarIntercityStartRequest (
        @SerializedName("plate")
        val plate: String,
        @SerializedName("startOdometer")
        val startOdometer: Double,
        @SerializedName("make")
        val make: String,
        @SerializedName("model")
        val model: String,
        @SerializedName("startLocation")
        val startLocation: TaxiLocationModel
)