package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CampusesModel(
        @SerializedName("id")
        val id: Long,
        @SerializedName("location")
        val location: LocationModel?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("companyId")
        val companyId: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("vtspPoiIdentity")
        val vtspPoiIdentity: String?,
        @SerializedName("isActive")
        val isActive: Boolean?
) : Serializable