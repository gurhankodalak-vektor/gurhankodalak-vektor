package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ReservationModel(
        @SerializedName("id")
        val id: Int,
        @SerializedName("creationTime")
        val creationTime: String,
        @SerializedName("startTime")
        val startTime: String,
        @SerializedName("endTime")
        val endTime: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("relatedAccountId")
        val relatedAccountId: Int,
        @SerializedName("status")
        var statusModel: ReservationStatus?,
        @SerializedName("canStart")
        val canStart: Boolean
) : Serializable {
        var status: ReservationStatus
        get() {
                return statusModel?:ReservationStatus.APPROVED
        }
        set(value) {

        }
}

enum class ReservationStatus {
        APPROVED,
        PENDING,
        CANCELLED,
        REJECTED
}