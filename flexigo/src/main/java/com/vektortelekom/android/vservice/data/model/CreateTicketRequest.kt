package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreateTicketRequest (
        @SerializedName("ticketType")
        val ticketType: String,
        @SerializedName("ticketDay")
        val ticketDay: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("routeId")
        val routeId: Long,
        @SerializedName("vehicleId")
        val vehicleId: Int,
        @SerializedName("fileUuids")
        val fileUuids: List<String>
) : Serializable