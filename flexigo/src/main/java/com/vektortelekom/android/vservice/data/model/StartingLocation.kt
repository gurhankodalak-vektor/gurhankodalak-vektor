package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class StartingLocation(
    @SerializedName("address")
    val address: Any,
    @SerializedName("id")
    val id: Any,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("referenceId")
    val referenceId: Any,
    @SerializedName("regionId")
    val regionId: Any
): Serializable