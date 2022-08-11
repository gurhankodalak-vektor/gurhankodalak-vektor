package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MessageModel (
        @SerializedName("creationTime")
        val creationTime: Long,
        @SerializedName("message")
        val message: String,
        @SerializedName("sentBy")
        val sentBy: String
) : Serializable