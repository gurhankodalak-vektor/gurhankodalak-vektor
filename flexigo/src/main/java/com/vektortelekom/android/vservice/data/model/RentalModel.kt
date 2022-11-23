package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.vshare_api_ktx.model.BillInfoModel
import com.vektor.vshare_api_ktx.model.RentalType
import java.io.Serializable

data class RentalModel (
    @SerializedName("id")
        val id: Int,
    @SerializedName("creationTime")
        val creationTime: String,
    @SerializedName("startDate")
        val startDate: String,
    @SerializedName("km")
        val km: Double?,
    @SerializedName("isFirstTime")
        val isFirstTime: Boolean,
    @SerializedName("isActive")
        val isActive: Boolean,
    @SerializedName("step")
        val step: RentalStatus,
    @SerializedName("minute")
        val minute: Int?,
    @SerializedName("totalAmount")
        val totalAmount: Double,
    @SerializedName("originalAmount")
        val originalAmount: Double,
    @SerializedName("remainingAmount")
        val remainingAmount: Double,
    @SerializedName("sapReference")
        val sapReference: String,
    @SerializedName("sapUpdateNeeded")
        val sapUpdateNeeded: Boolean,
    @SerializedName("vehicleDeliveredDate")
        val vehicleDeliveredDate: String,
    @SerializedName("isOutOfServiceArea")
        val isOutOfServiceArea: Boolean,
    @SerializedName("rentalType")
        val rentalType: String?,
    @SerializedName("deviceVerified")
        val deviceVerified: Boolean,
    @SerializedName("kabisEmailSentCount")
        val kabisEmailSentCount: Int,
    @SerializedName("rentalStep")
        val rentalStep: String,
    @SerializedName("previousRentalStep")
        val previousRentalStep: String,
    @SerializedName("billInfo")
        val billInfo: BillInfoModel?,
    @SerializedName("locationPhotoUuid")
        val locationPhotoUuid: String?,
    @SerializedName("locationDescription")
        val locationDescription: String?,
    @SerializedName("workflowType")
        val workflowType: ReservationWorkFlowType?

): Serializable

enum class RentalStatus {
    AVAILABLE,
    CAR_SELECTED_FOR_RENT,
    NOT_STARTED,
    WAITING_PROVISION_PAYMENT,
    PREPARING_VEHICLE,
    VEHICLE_ON_DELIVERY,
    WAITING_CUSTOMER_TO_VEHICLE,
    WAITING_RENTAL_START,
    RENTAL_IN_PROGRESS,
    WAITING_FINAL_PAYMENT,
    FINISHED,
    CANCELED,
    undefined
}