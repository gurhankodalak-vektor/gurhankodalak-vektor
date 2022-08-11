package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RouteDraftsModel(
        @SerializedName("draftVersion")
        val draftVersion: DraftVersion,
        @SerializedName("stations")
        val stations: MutableList<StationModel>
): Serializable

data class DraftVersion(
        @SerializedName("id")
        val id: Int,
        @SerializedName("isActive")
        val isActive: Boolean,
        @SerializedName("isOptimizationRoute")
        val isOptimizationRoute: Boolean,
        @SerializedName("routeDetail")
        val routeDetail: RouteDetail,
        @SerializedName("routeId")
        val routeId: Long
): Serializable