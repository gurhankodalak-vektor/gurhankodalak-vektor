package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class PastUseModel (
    val id: Int,
    val title: String,
    val type: PastUseType,
    val date: String,
    val duration: Int,
    val startLocation: TaxiLocationModel,
    val endLocation: TaxiLocationModel
)

enum class PastUseType {
    @SerializedName("POOLCAR")
    POOLCAR,
    @SerializedName("FLEXIRIDE")
    FLEXIRIDE,
    @SerializedName("TAXI")
    TAXI,
}