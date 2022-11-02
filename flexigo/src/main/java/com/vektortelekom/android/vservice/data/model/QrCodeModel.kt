package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class QrCodeModel (
        @SerializedName("response")
        val response: String,
        @SerializedName("result")
        val result: String
) : Serializable

data class ResponseModel (
        @SerializedName("response")
        val response: String
) : Serializable