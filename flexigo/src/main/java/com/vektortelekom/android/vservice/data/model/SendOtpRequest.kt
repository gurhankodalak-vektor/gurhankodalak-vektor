package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SendOtpRequest (
        @SerializedName("phoneNumber")
        val phoneNumber: String,
        @SerializedName("pinCode")
        val pinCode: String
) : Serializable