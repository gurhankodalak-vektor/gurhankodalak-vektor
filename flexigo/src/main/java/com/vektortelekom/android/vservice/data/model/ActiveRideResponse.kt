package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

data class ActiveRideResponse(
    @SerializedName("activeRide")
    val activeRide: Boolean,
    @SerializedName("eta")
    val eta: Int?,
    @SerializedName("firstDepartureDate")
    val firstDepartureDate: Long,
    @SerializedName("firstLeg")
    val firstLeg: Boolean,
    @SerializedName("fromTerminalReferenceId")
    val fromTerminalReferenceId: Int,
    @SerializedName("fromTerminalReferenceIds")
    val fromTerminalReferenceIds: Any,
    @SerializedName("fromType")
    val fromType: String,
    @SerializedName("isDriver")
    val isDriver: Boolean,
    @SerializedName("name")
    val name: String,
    @SerializedName("notUsing")
    val notUsing: Boolean,
    @SerializedName("reserved")
    val reserved: Boolean,
    @SerializedName("returnDepartureDate")
    val returnDepartureDate: Long,
    @SerializedName("routeDirection")
    val routeDirection: Any,
    @SerializedName("routeId")
    val routeId: Int,
    @SerializedName("routeInstanceId")
    val routeInstanceId: Any,
    @SerializedName("routeName")
    val routeName: String,
    @SerializedName("stationId")
    val stationId: Int,
    @SerializedName("templateId")
    val templateId: Int,
    @SerializedName("toTerminalReferenceId")
    val toTerminalReferenceId: Any,
    @SerializedName("toTerminalReferenceIds")
    val toTerminalReferenceIds: Any,
    @SerializedName("toType")
    val toType: String,
    @SerializedName("vehicleId")
    val vehicleId: Any,
    @SerializedName("vehiclePlate")
    val vehiclePlate: Any,
    @SerializedName("walkingPath")
    val walkingPath: WalkingPath,
    @SerializedName("workgroupDirection")
    val workgroupDirection: String,
    @SerializedName("workgroupInstanceId")
    val workgroupInstanceId: Int,
    @SerializedName("workgroupStatus")
    val workgroupStatus: String,
    @SerializedName("workgroupType")
    val workgroupType: String
): BaseResponse(), Serializable