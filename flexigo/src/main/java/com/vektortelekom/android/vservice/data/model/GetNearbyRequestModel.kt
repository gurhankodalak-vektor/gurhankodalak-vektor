package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GetNearbyRequestModel (
        @SerializedName("response")
        val response: Boolean
) : Serializable