package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GetDestinationsResponse (
        @SerializedName("response")
        val response: List<DestinationModel>
) : Serializable