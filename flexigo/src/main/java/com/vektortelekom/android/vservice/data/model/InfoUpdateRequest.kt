package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class InfoUpdateRequest (
        @SerializedName("phoneNumber")
        val phoneNumber: String
) : Serializable