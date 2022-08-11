package com.vektortelekom.android.vservice.data.model.workgroup

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WorkGroupShift(
    @SerializedName("arrivalHour")
    val arrivalHour: Int?,
    @SerializedName("departureHour")
    val departureHour: Int?,
    @SerializedName("friday")
    val friday: Boolean,
    @SerializedName("monday")
    val monday: Boolean,
    @SerializedName("readyHour")
    val readyHour: Int?,
    @SerializedName("returnArrivalHour")
    val returnArrivalHour: Int?,
    @SerializedName("returnDepartureHour")
    val returnDepartureHour: Int?,
    @SerializedName("returnReadyHour")
    val returnReadyHour: Int?,
    @SerializedName("saturday")
    val saturday: Boolean,
    @SerializedName("sunday")
    val sunday: Boolean,
    @SerializedName("thursday")
    val thursday: Boolean,
    @SerializedName("tuesday")
    val tuesday: Boolean,
    @SerializedName("wednesday")
    val wednesday: Boolean
): Serializable