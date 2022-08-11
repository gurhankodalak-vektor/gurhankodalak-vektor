package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DestinationModel(
        @SerializedName("id")
        val id: Long,
        @SerializedName("location")
        val location: LocationModel?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("routeGroupId")
        val routeGroupId: String?,
        @SerializedName("companyId")
        val companyId: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("vtspDn")
        val vtspDn: String?,
        @SerializedName("vtspPoiIdentity")
        val vtspPoiIdentity: String?,
        @SerializedName("vtspGroupId")
        val vtspGroupId: String?,
        @SerializedName("vtspPoiId")
        val vtspPoiId: Long?,
        @SerializedName("mapCoords")
        val mapCoords: List<List<Double>>?,
        @SerializedName("isActive")
        val isActive: Boolean?,
        @SerializedName("isBranch")
        val isBranch: Boolean?,
        @SerializedName("ownerId")
        val ownerId: String?,
        @SerializedName("polygon")
        val polygon: List<List<Double>>
) : Serializable