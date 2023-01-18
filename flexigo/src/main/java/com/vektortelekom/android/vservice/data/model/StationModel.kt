package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
data class StationModel (
        @SerializedName("id")
        val id: Long,
        @SerializedName("name")
        val name: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("address")
        val address: String,
        @SerializedName("routeId")
        val routeId: Long,
        @SerializedName("vtspPoiId")
        val vtspPoiId: Long,
        @SerializedName("location")
        val location: LocationModel,
        @SerializedName("routeIndex")
        val routeIndex: Int,
        @SerializedName("companyId")
        val companyId: Long,
        @SerializedName("status")
        val status: Boolean,
        @SerializedName("notEmpty")
        val notEmpty: Boolean,
        @SerializedName("active")
        val active: Boolean,
        @SerializedName("empty")
        val empty: Boolean,
        @SerializedName("durationInMin")
        val durationInMin: Double,
        @SerializedName("expectedArrivalHour")
        val expectedArrivalHour: Int?,
        @SerializedName("distanceInMeter")
        val distanceInMeter: Int?,
        @Expose
        var route: RouteResponse?,
        @Expose
        var route2: RouteDetailResponse?
) : Serializable