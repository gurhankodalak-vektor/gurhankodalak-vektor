package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class GetShiftRequest (
    @SerializedName("destinationId")
    val destinationId: Long
)