package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReadQrCodeRequest (
        @SerializedName("routeQrCode")
        val routeQrCode: String,
        @SerializedName("latitude")
        val latitude: Double,
        @SerializedName("longitude")
        val longitude: Double
): Serializable