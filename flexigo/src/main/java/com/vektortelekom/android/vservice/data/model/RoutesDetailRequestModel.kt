package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RoutesDetailRequestModel (
    @SerializedName("routeIds")
    val routeIds: List<Long>,
    @SerializedName("day")
    val day: String
) : Serializable