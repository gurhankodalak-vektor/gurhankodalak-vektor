package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RouteResponse(
        @SerializedName("route")
        val route: RouteModel,
        @SerializedName("persons")
        val persons: List<PersonsModel?>,
        @SerializedName("pathPersonnelStations")
        val pathPersonnelStations: Any?,
        @SerializedName("returnPathPersonnelStations")
        val returnPathPersonnelStations: Any?

) : Serializable
