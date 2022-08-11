package com.vektortelekom.android.vservice.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FlexirideOffer (
        @SerializedName("startTime")
        val startTime: String,
        @SerializedName("finishTime")
        val finishTime: String,
        @SerializedName("startLocation")
        val startLocation: String,
        @SerializedName("endLocation")
        val finishLocation: String,
        @SerializedName("walkingMin")
        val walkingMin: Int,
        @SerializedName("price")
        val price: Double,
        @SerializedName("isBestOffer")
        val isBestOffer: Boolean,
        @SerializedName("startLatLng")
        val startLatLng: LatLng,
        @SerializedName("finishLatLng")
        val finishLatLng: LatLng
): Serializable