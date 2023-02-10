package com.vektortelekom.android.vservice.data.response

import com.google.gson.annotations.SerializedName
import com.vektortelekom.android.vservice.data.model.LocationModel
import java.io.Serializable

data class MyCampusResponse(
    @SerializedName("companyId")
    val companyId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("isBranch")
    val isBranch: Boolean,
    @SerializedName("location")
    val location: LocationModel,
    @SerializedName("mapCoords")
    val mapCoords: List<List<Double>>,
    @SerializedName("name")
    val name: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("vtspPoiIdentity")
    val vtspPoiIdentity: String
): Serializable