package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RouteStopRequest (
        @SerializedName("from")
        val from: SearchRequestModel,
        @SerializedName("whereto")
        val whereto: SearchRequestModel,
        @SerializedName("shiftId")
        val shiftId: Int?,
        @SerializedName("workgroupInstanceId")
        val workgroupInstanceId: Long
): Serializable

data class SearchRequestModel (
        @SerializedName("lat")
        val lat: Double?,
        @SerializedName("lng")
        val lng: Double?,
        @SerializedName("destinationId")
        var destinationId: Long?
) : Serializable