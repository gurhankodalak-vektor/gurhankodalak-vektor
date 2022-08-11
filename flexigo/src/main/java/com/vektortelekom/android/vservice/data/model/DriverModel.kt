package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DriverModel (
        @SerializedName("name")
        val name: String?,
        @SerializedName("surname")
        val surname: String?,
        @SerializedName("phoneNumber")
        val phoneNumber: String,
        @SerializedName("email")
        val email: String?,
        @SerializedName("gender")
        val gender: String?
) : Serializable