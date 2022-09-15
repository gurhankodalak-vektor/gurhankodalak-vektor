package com.vektortelekom.android.vservice.data.model

import android.text.SpannableString
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LocationModel (
        @SerializedName("id")
        val id: Int,
        @SerializedName("latitude")
        val latitude: Double,
        @SerializedName("longitude")
        val longitude: Double,
        @SerializedName("address")
        val address: String,
        @SerializedName("regionId")
        val regionId: Int?,
        @SerializedName("referenceId")
        val referenceId: Int?,
        @SerializedName("notValid")
        val notValid: Boolean
): Serializable