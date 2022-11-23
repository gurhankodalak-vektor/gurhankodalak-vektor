package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CarPoolPreferencesModel (
        @SerializedName("id")
        val id: Long?,
        @SerializedName("personnelId")
        val personnelId: Long?,
        @SerializedName("isDriver")
        var isDriver: Boolean?,
        @SerializedName("isRider")
        val isRider: Boolean?,
        @SerializedName("arrivalHour")
        val arrivalHour: Int?,
        @SerializedName("departureHour")
        val departureHour: Int?
): Serializable