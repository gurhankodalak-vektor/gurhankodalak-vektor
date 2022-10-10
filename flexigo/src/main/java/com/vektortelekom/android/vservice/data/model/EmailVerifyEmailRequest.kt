package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EmailVerifyEmailRequest (
        @SerializedName("name")
        val name: String?,
        @SerializedName("surname")
        val surname: String?,
        @SerializedName("email")
        val email: String?,
        @SerializedName("password")
        val password: String?,
        @SerializedName("companyAuthCode")
        val companyAuthCode: String?,
        @SerializedName("verificationCode")
        val verificationCode: String?
) : Serializable