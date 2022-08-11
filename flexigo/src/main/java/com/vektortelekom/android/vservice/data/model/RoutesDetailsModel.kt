package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class RoutesDetailsModel (
        @SerializedName("routeIds")
        val routeIds : Set<Long>
)