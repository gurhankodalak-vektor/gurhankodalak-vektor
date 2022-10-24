package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CarPoolPreferencesRequest (
        @SerializedName("isDriver")
        val isDriver: Boolean,
        @SerializedName("isRider")
        val isRider: Boolean
) : Serializable