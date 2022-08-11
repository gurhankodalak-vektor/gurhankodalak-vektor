package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SearchRouteRequest (
        @SerializedName("searchText")
        val searchText: String
): Serializable