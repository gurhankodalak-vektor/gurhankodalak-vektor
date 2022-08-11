package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SearchRouteResponse (
        @SerializedName("response")
        val response: List<RouteModel>
): Serializable