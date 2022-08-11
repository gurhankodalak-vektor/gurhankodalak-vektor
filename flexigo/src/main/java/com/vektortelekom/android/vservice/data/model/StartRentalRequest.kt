package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class StartRentalRequest (
        @SerializedName("startOdometer")
        val startOdometer: Double? = null
): Serializable