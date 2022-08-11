package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RouteDetailResponse(
        @SerializedName("response")
        val response: RouteModel
) : Serializable
