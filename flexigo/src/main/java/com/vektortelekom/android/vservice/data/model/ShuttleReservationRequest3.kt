package com.vektortelekom.android.vservice.data.model

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import org.json.JSONArray

data class ShuttleReservationRequest3(
    @SerializedName("reservationDay")
    var reservationDay: String,
    @SerializedName("reservationDayEnd")
    var reservationDayEnd: String?,
    @SerializedName("workgroupInstanceId")
    val workgroupInstanceId: Long,
    @SerializedName("routeId")
    val routeId: Long,
    @SerializedName("useFirstLeg")
    var useFirstLeg: Boolean?,
    @SerializedName("firstLegStationId")
    val firstLegStationId: Long?,
    @SerializedName("useReturnLeg")
    var useReturnLeg: Boolean?,
    @SerializedName("returnLegStationId")
    val returnLegStationId: Long?,
    @SerializedName("dayOfWeeks")
    val dayOfWeeks: JsonArray?

)