package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PersonsModel (
        @SerializedName("id")
        val id: Long,
        @SerializedName("name")
        val name: String,
        @SerializedName("surname")
        val surname: String,
        @SerializedName("fullName")
        val fullName: String,
        @SerializedName("phoneNumber")
        val phoneNumber: String?

) : Serializable{}
