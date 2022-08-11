package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ChangePasswordRequest (
        @SerializedName("password")
        val password: String,
        @SerializedName("newPassword")
        val newPassword: String,
        @SerializedName("newPassword2")
        val newPassword2: String
):Serializable