package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TicketModel (
        @SerializedName("id")
        val id: Int,
        @SerializedName("creationTime")
        val creationTime: Long,
        @SerializedName("ticketStatus")
        val ticketStatus: TicketStatus,
        @SerializedName("localizedTicketStatus")
        val localizedTicketStatus: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("ticketType")
        val ticketType: String,
        @SerializedName("ticketDay")
        val ticketDay: Int,
        @SerializedName("closedTime")
        val closedTime: Long?,
        @SerializedName("resolutionCategory")
        val resolutionCategory: String?,
        @SerializedName("logs")
        val logs: List<TicketLog>?
) : Serializable

enum class TicketStatus {
        @SerializedName("OPEN")
        OPEN,
        @SerializedName("PENDING")
        PENDING,
        @SerializedName("INPROGRESS")
        INPROGRESS,
        @SerializedName("RESOLVED")
        RESOLVED,
        @SerializedName("REOPENED")
        REOPENED,
        @SerializedName("CLOSED")
        CLOSED,
        @SerializedName("OPEN")
        undefined
}

data class TicketLog(
        @SerializedName("creationTime")
        val creationTime: Long?,
        @SerializedName("logDescription")
        val logDescription: String?
): Serializable