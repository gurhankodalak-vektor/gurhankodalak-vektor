package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RouteDetail(
        @SerializedName("mapPersonnelStations")
        val mapPersonnelStations: MapPersonnelStations,
        @SerializedName("path")
        val path: Path,
        @SerializedName("returnPath")
        val returnPath: Path
): Serializable