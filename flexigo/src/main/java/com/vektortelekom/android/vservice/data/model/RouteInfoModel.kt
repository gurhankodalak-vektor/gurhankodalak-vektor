package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RouteInfoModel (
        @SerializedName("route_name")
        val route_name: String,
        @SerializedName("route_title")
        val route_title: String,
        @SerializedName("driver_name")
        val driver_name: String,
        @SerializedName("driver_surname")
        val driver_surname: String,
        @SerializedName("plate_id")
        val plate_id: String,
        @SerializedName("destination_name")
        val destination_name: String,
        @SerializedName("destination_latitude")
        val destination_latitude: Double,
        @SerializedName("destination_longitude")
        val destination_longitude: Double,
        @SerializedName("destination_address")
        val destination_address: String,
        @SerializedName("make")
        val make: String,
        @SerializedName("model")
        val model: String?,
        @SerializedName("vehicle_year")
        val vehicle_year: Int,
        @SerializedName("phone_number")
        val phone_number: String
) : Serializable