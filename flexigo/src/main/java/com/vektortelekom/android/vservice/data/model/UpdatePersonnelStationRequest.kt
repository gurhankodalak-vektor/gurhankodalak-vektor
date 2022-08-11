package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UpdatePersonnelStationRequest(
        @SerializedName("id")
        val id: Long
) : Serializable