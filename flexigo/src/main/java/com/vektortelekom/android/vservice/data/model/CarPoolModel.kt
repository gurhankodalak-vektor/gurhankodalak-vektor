package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CarPoolModel (
        @SerializedName("matchedRiders")
        val matchedRiders: List<CarPoolListModel>,
        @SerializedName("matchedDrivers")
        val matchedDrivers: List<CarPoolListModel>,
        @SerializedName("carPoolPreferences")
        val carPoolPreferences: CarPoolPreferencesModel?,
        @SerializedName("closeRiders")
        val closeRiders: List<CarPoolListModel>,
        @SerializedName("closeDrivers")
        val closeDrivers: List<CarPoolListModel>,
        @SerializedName("approvedRiders")
        val approvedRiders: List<CarPoolListModel>?,
        @SerializedName("ridingWith")
        val ridingWith: CarPoolListModel?
) : Serializable

