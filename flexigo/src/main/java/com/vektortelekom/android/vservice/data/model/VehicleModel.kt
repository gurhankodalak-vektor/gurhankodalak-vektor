package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VehicleModel (
        @SerializedName("id")
        val id: Int,
        @SerializedName("plateId")
        val plateId: String,
        @SerializedName("name")
        val name: String?,
        @SerializedName("make")
        val make: String?,
        @SerializedName("capacity")
        val capacity: Int,
        @SerializedName("deviceStatus")
        val deviceStatus: String
) : Serializable