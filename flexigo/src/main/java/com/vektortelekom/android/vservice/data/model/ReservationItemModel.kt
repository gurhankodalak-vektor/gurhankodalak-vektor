package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReservationItemModel(
        @SerializedName("reservation")
        val reservation: ReservationModel,
        @SerializedName("vehicle")
        val vehicle: PoolCarVehicleModel,
        @SerializedName("park")
        val park: ParkModel
): Serializable