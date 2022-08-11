package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VektorEnumResponse (
        @SerializedName("response")
        val response: List<VektorEnum>
): Serializable