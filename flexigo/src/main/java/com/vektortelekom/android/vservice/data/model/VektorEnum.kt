package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VektorEnum (
        @SerializedName("value")
        val value: String,
        @SerializedName("text")
        val text: String
): Serializable