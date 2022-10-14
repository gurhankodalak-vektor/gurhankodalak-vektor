package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UpdatePersonnelCampusRequest(
        @SerializedName("destinationId")
        val destinationId: Long
) : Serializable