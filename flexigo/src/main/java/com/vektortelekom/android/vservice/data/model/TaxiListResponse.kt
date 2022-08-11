package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TaxiListResponse(
        @SerializedName("response")
        val response: List<TaxiUsage>
) : Serializable