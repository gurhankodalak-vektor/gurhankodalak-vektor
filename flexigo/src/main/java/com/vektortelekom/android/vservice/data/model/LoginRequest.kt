package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LoginRequest(
        @SerializedName("email")
        val email: String,
        @SerializedName("password")
        val password : String,
        @SerializedName("mobilePhoneId")
        val mobilePhoneId: String?,
        @SerializedName("mobilePlatform")
        val mobilePlatform: String,
        @SerializedName("langCode")
        val langCode: String
) : Serializable