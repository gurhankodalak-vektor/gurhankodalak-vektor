package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UpdateFirebaseTokenRequest (
        @SerializedName("mobilePhoneId")
        val mobilePhoneId: String,
        @SerializedName("mobilePlatform")
        val mobilePlatform: String = "android"
): Serializable