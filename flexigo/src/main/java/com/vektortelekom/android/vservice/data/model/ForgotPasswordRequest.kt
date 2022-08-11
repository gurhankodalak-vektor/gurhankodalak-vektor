package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ForgotPasswordRequest(
        @SerializedName("email")
        val email: String
) : Serializable