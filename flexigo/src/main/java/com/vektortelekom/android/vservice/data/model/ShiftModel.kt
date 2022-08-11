package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ShiftModel (
        @SerializedName("id")
        val id: Int?,
        @SerializedName("name")
        val name: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("startHour")
        val startHour: Int?,
        @SerializedName("startArrival")
        val startArrival: Int?,
        @SerializedName("endHour")
        val endHour: Int,
        @SerializedName("endLeave")
        val endLeave: Int,
        @SerializedName("endCheckStart")
        val endCheckStart: Int
) : Serializable