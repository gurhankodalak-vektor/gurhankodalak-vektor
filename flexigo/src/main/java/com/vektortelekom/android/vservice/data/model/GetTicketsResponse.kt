package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GetTicketsResponse(
        @SerializedName("response")
        val response: List<TicketModel>
): Serializable