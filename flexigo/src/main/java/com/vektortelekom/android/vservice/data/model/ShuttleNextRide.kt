package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse

data class ShuttleNextRide (
        @SerializedName("templateId")
        val templateId: Long,
        @SerializedName("workgroupInstanceId")
        val workgroupInstanceId: Long,
        @SerializedName("firstDepartureDate")
        val firstDepartureDate: Long,
        @SerializedName("returnDepartureDate")
        val returnDepartureDate: Long,
        @SerializedName("workgroupStatus")
        val workgroupStatus: WorkgroupStatus?,
        @SerializedName("name")
        val name: String,
        @SerializedName("routeId")
        val routeId: Long?,
        @SerializedName("routeName")
        val routeName: String?,
        @SerializedName("vehicleId")
        val vehicleId: Long?,
        @SerializedName("vehiclePlate")
        val vehiclePlate: String?,
        @SerializedName("workgroupType")
        val workgroupType: String?,
        @SerializedName("stationId")
        val stationId: Long?,
        @SerializedName("fromType")
        val fromType: FromToType,
        @SerializedName("fromTerminalReferenceId")
        val fromTerminalReferenceId: Long?,
        @SerializedName("toType")
        val toType: FromToType,
        @SerializedName("toTerminalReferenceId")
        val toTerminalReferenceId: Long?,
        @SerializedName("workgroupDirection")
        val workgroupDirection: WorkgroupDirection?,
        @SerializedName("firstLeg")
        val firstLeg: Boolean,
        @SerializedName("reserved")
        val reserved: Boolean,
        @SerializedName("notUsing")
        val notUsing: Boolean,
        @SerializedName("isDriver")
        val isDriver: Boolean,
        @SerializedName("activeRide")
        val activeRide: Boolean,
        @SerializedName("eta")
        val eta: Int?,
        @SerializedName("usersCanDemand")
        val usersCanDemand: Boolean?
): BaseResponse()

enum class WorkgroupStatus{
    @SerializedName("PENDING_DEMAND")
    PENDING_DEMAND,
    @SerializedName("PENDING_ASSIGNMENT")
    PENDING_ASSIGNMENT,
    @SerializedName("PENDING_PLANNING")
    PENDING_PLANNING,
    @SerializedName("CANCELLED")
    CANCELLED,
    @SerializedName("ACTIVE")
    ACTIVE
}

enum class FromToType{
    @SerializedName("CAMPUS")
    CAMPUS,
    @SerializedName("PERSONNEL_SHUTTLE_STOP")
    PERSONNEL_SHUTTLE_STOP,
    @SerializedName("PERSONNEL_HOME_ADDRESS")
    PERSONNEL_HOME_ADDRESS,
    @SerializedName("PERSONNEL_WORK_LOCATION")
    PERSONNEL_WORK_LOCATION
}

enum class WorkgroupDirection{
    @SerializedName("ROUND_TRIP")
    ROUND_TRIP,
    @SerializedName("ONE_WAY")
    ONE_WAY
}