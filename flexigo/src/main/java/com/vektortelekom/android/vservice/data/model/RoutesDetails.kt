package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class RoutesDetails (
        @SerializedName("response")
        val response: List<RouteModel>
)