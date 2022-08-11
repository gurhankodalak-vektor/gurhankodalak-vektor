package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TicketTypeModel (
        @SerializedName("name")
        val name: String,
        @SerializedName("isSelected")
        val isSelected: Boolean,
        @SerializedName("key")
        val key: String
) : Serializable