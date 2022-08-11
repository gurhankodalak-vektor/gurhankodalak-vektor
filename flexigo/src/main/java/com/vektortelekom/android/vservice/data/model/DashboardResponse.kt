package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DashboardResponse(
        @SerializedName("response")
        val response: DashboardInnerResponse
) : Serializable

data class DashboardInnerResponse(
        @SerializedName("dashboard")
        val dashboard: List<DashboardModel>,
        @SerializedName("notifications")
        val notifications: MutableList<NotificationModel>,
        @SerializedName("messages")
        val messages: MutableList<MessageModel>
) : Serializable