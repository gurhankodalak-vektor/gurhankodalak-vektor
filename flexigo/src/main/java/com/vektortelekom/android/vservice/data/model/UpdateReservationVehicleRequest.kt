package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class UpdateReservationVehicleRequest (
        @SerializedName("qrCode")
        val qrCode: String
)