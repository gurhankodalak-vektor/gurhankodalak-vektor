package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DashboardModel (
        @SerializedName("type")
        val type: DashboardItemType,
        @SerializedName("title")
        val title: String,
        @SerializedName("subTitle")
        var subTitle: String?,
        @SerializedName("info")
        val info: String?,
        @SerializedName("iconName")
        val iconName: String,
        @SerializedName("tintColor")
        val tintColor: String,
        @SerializedName("userPermission")
        val userPermission: Boolean,
        @SerializedName("isPoolCarReservationRequired")
        val isPoolCarReservationRequired : Boolean?
): Serializable

enum class DashboardItemType(val type: String) {
    Shuttle("Shuttle"),
    PoolCar("PoolCar"),
    Taxi("Taxi"),
    FlexiRide("FlexiRide"),
    Calendar("Calendar"),
    ReportComplaints("ReportComplaints"),
    PastUses("PastUses")
}