package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GetTicketTypesResponse(
        @SerializedName("response")
        val response: List<TicketTypeModel>
) : Serializable