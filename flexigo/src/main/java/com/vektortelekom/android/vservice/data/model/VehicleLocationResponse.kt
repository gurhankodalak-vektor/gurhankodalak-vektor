package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

data class VehicleLocationResponse (
        @SerializedName("response")
        val response: VehicleLocationModel
): BaseResponse(), Serializable

data class VehicleLocationModel (
        @SerializedName("GEOZONES_CROSSED")
        val GEOZONES_CROSSED: String?,
        @SerializedName("OPERATING_MODE")
        val OPERATING_MODE: String?,
        @SerializedName("TRIP_START_LATITUDE")
        val TRIP_START_LATITUDE: String?,
        @SerializedName("VEHICLE_TRIP_END")
        val VEHICLE_TRIP_END: String?,
        @SerializedName("TRIP_DISTANCE_TRAVELED")
        val TRIP_DISTANCE_TRAVELED: String?,
        @SerializedName("GEOZONES_CROSSED_ID")
        val GEOZONES_CROSSED_ID: String?,
        @SerializedName("TRIP_MAX_SPEED")
        val TRIP_MAX_SPEED: String?,
        @SerializedName("GEO_ADDRESS")
        val GEO_ADDRESS: String?,
        @SerializedName("TOW_COUNT")
        val TOW_COUNT: String?,
        @SerializedName("TRIP_START_ANALOG_INPUT2")
        val TRIP_START_ANALOG_INPUT2: String?,
        @SerializedName("GEOZONES_COMPASSING_ID")
        val GEOZONES_COMPASSING_ID: String?,
        @SerializedName("CANBUS_ODOMETER")
        val CANBUS_ODOMETER: String?,
        @SerializedName("POSTAL_CODE")
        val POSTAL_CODE: String?,
        @SerializedName("TRIP_AVERAGE_SPEED")
        val TRIP_AVERAGE_SPEED: String?,
        @SerializedName("TRIP_START_DIRECTION")
        val TRIP_START_DIRECTION: String?,
        @SerializedName("TRIP_START_LONGITUDE")
        val TRIP_START_LONGITUDE: String?,
        @SerializedName("GEO_COUNTRY")
        val GEO_COUNTRY: String?,
        @SerializedName("VEHICLE_TRIP_DURATION")
        val VEHICLE_TRIP_DURATION: String?,
        @SerializedName("IDLING")
        val IDLING: String?,
        @SerializedName("PERSONNEL_CARD_COUNT")
        val PERSONNEL_CARD_COUNT: String?,
        @SerializedName("GEOROUTES_ON_ID")
        val GEOROUTES_ON_ID: String?,
        @SerializedName("GEOZONES_COMPASSING")
        val GEOZONES_COMPASSING: String?,
        @SerializedName("TRIP_START_DIGITAL_INPUTS")
        val TRIP_START_DIGITAL_INPUTS: String?,
        @SerializedName("LAST_CONNECTION_DATE")
        val LAST_CONNECTION_DATE: String?,
        @SerializedName("VEHICLE_TRIP_START")
        val VEHICLE_TRIP_START: String?,
        @SerializedName("ROAD_SPEED_LIMIT")
        val ROAD_SPEED_LIMIT: String?,
        @SerializedName("ROAD_TYPE")
        val ROAD_TYPE: String?,
        @SerializedName("IDLE_DURATION_AFTER_IGNITION1")
        val IDLE_DURATION_AFTER_IGNITION1: String?,
        @SerializedName("DISTANCE_TRAVELED")
        val DISTANCE_TRAVELED: String?,
        @SerializedName("UNIT_OPERATION")
        val UNIT_OPERATION: String?,
        @SerializedName("TOW_START")
        val TOW_START: String?,
        @SerializedName("PLATE")
        val PLATE: String?,
        @SerializedName("TRIP_TOTAL_SPEED_VIOLATION_DURATION")
        val TRIP_TOTAL_SPEED_VIOLATION_DURATION: String?,
        @SerializedName("assetId")
        val assetId: String?,
        @SerializedName("LATITUDE")
        val LATITUDE: String?,
        @SerializedName("LONGITUDE")
        val LONGITUDE: String?,
        @SerializedName("DIRECTION")
        val DIRECTION: String?,
        @SerializedName("TEMP_SPEED")
        val TEMP_SPEED: String?,
        @SerializedName("VEHICLE_IGNITION")
        val VEHICLE_IGNITION: String?,
        @SerializedName("ADDRESS")
        val ADDRESS: String?
) : Serializable