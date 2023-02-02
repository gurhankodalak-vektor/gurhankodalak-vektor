package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class WorkgroupDemandRequest (
        @SerializedName("workgroupInstanceId")
        val workgroupInstanceId: Long,
        @SerializedName("stationId")
        val stationId: Long?,
        @SerializedName("location")
        val location: LocationModel2?,
        @SerializedName("destinationId")
        val destinationId: Long?
)

data class LocationModel2 (
        @SerializedName("latitude")
        val latitude: Double?,
        @SerializedName("longitude")
        val longitude: Double?
)