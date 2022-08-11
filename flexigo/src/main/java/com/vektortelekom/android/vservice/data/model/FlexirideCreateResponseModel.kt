package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.vshare_api_ktx.model.DeliveryAddressModel2
import java.io.Serializable

data class FlexirideCreateResponseModel(
        @SerializedName("id")
        val id: Int,
        @SerializedName("fromLocation")
        val fromLocation: TaxiLocationModel,
        @SerializedName("toLocation")
        val toLocation: TaxiLocationModel,
        @SerializedName("requestTime")
        val requestTime: String,
        @SerializedName("passengerCount")
        val passengerCount: Int,
        @SerializedName("requiredChildSeats")
        val requiredChildSeats: Int,
        @SerializedName("distanceInMeter")
        val distanceInMeter: Double,
        @SerializedName("travelTimeInMinute")
        val travelTimeInMinute: Double,
        @SerializedName("estimatedArrivalTime")
        val estimatedArrivalTime: String,
        @SerializedName("vehicle")
        val vehicle: FlexirideVehicle?,
        @SerializedName("driver")
        val driver: FlexirideDriver?,
        @SerializedName("status")
        var status: FlexirideStatus?
) : Serializable

data class FlexirideDriver(
        @SerializedName("id")
        val id: Int?,
        @SerializedName("fullName")
        val fullName: String?,
        @SerializedName("location")
        val location: DeliveryAddressModel2?,
        @SerializedName("mobile")
        val mobile: String? = null
): Serializable

data class FlexirideVehicle(
        @SerializedName("id")
        val id: Int?,
        @SerializedName("plate")
        val plate: String?
): Serializable

enum class FlexirideStatus {
        @SerializedName("PENDING")
        PENDING,
        @SerializedName("APPROVED")
        APPROVED,
        @SerializedName("PLANNED")
        PLANNED,
        @SerializedName("REJECTED")
        REJECTED,
        @SerializedName("CANCELLED")
        CANCELLED,
        @SerializedName("COMPLETED")
        COMPLETED
}