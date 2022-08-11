package com.vektortelekom.android.vservice.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class PoolcarAndFlexirideModel (
        @SerializedName("id")
        var id: Int? = null,
        @SerializedName("creationTime")
        var creationTime: String? = null,
        @SerializedName("status")
        var status:FlexirideAndPoolcarStatus? = null,
        @SerializedName("requestType")
        var requestType: FlexirideAndPoolcarRequestType? = null,
        @SerializedName("canStart")
        var canStart: Boolean? = null,
        @SerializedName("park")
        var park: ParkModel? = null,
        @SerializedName("fromLocation")
        var fromLocation: TaxiLocationModel? = null,
        @SerializedName("fromLocationDescription")
        var fromLocationDescription: String? = null,
        @SerializedName("toLocation")
        var toLocation: TaxiLocationModel? = null,
        @SerializedName("toLocationDescription")
        var toLocationDescription: String? = null,
        @SerializedName("distanceInMeter")
        var distanceInMeter: String? = null,
        @SerializedName("flexirideRequest")
        var flexirideRequest: FlexirideRequestModel? = null,
        @SerializedName("reservation")
        var reservation: PoolcarReservationModel? = null,
        @SerializedName("vehicle")
        var vehicle: PoolCarVehicleModel? = null,
        @SerializedName("driver")
        var driver: FlexirideDriver? = null,
        @SerializedName("travelDestinationType")
        val travelDestinationType: TravelDestinationType? = null,
        @SerializedName("additionalRiders")
        var additionalRiders: List<Int>? = null
): Parcelable

enum class FlexirideAndPoolcarStatus {
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
    @SerializedName("FINISHED")
    FINISHED
}

enum class FlexirideAndPoolcarRequestType {
    @SerializedName("FLEXIRIDE")
    FLEXIRIDE,
    @SerializedName("POOL_CAR")
    POOL_CAR,
    @SerializedName("GUESTRIDE")
    GUESTRIDE,
    @SerializedName("PARTNER_CAR_REQUEST")
    PARTNER_CAR_REQUEST
}

enum class ReservationWorkFlowType {
    @SerializedName("DEFAULT")
    DEFAULT,
    @SerializedName("LIGHT")
    LIGHT
}

@Parcelize
data class FlexirideRequestModel(
        @SerializedName("requestedPickupTime")
        var requestedPickupTime: String? = null,
        @SerializedName("requestedDeliveryTime")
        var requestedDeliveryTime: String? = null,
        @SerializedName("travelTimeInMinute")
        var travelTimeInMinute: Double? = null,
        @SerializedName("passengerCount")
        var passengerCount: Int? = null,
        @SerializedName("requiredChildSeats")
        var requiredChildSeats: Int? = null,
        @SerializedName("vehicleCatalogId")
        var vehicleCatalogId: Int? = null,
        @SerializedName("priceModelId")
        var priceModelId: Int? = null,
        @SerializedName("fullName")
        var fullName: String? = null,
        @SerializedName("mobile")
        var mobile: String?= null,
        @SerializedName("startPoiId")
        var startPoiId: Int? = null,
        @SerializedName("endPoiId")
        var endPoiId: Int? = null,
        @SerializedName("destinationsText")
        var destinationsText: String? = null,
        @SerializedName("vehiclePlate")
        var vehiclePlate: String? = null,
        @SerializedName("startOdometer")
        var startOdometer: Double? = null,
        @SerializedName("vehicleMake")
        var vehicleMake: String? = null,
        @SerializedName("vehicleModel")
        var vehicleModel: String? = null,
        @SerializedName("workflowType")
        var workflowType: ReservationWorkFlowType? = null
): Parcelable

@Parcelize
data class PoolcarReservationModel(
        @SerializedName("startTime")
        val startTime: String? = null,
        @SerializedName("endTime")
        val endTime: String? = null,
        @SerializedName("creationTime")
        var creationTime: String? = null,
        @SerializedName("description")
        val description: String? = null,
        @SerializedName("reason")
        val reason: String? = null
) : Parcelable

enum class TravelDestinationType {
    @SerializedName("LOCAL")
    LOCAL,
    @SerializedName("INTERCITY")
    INTERCITY
}