package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.vshare_api_ktx.model.DeliveryAddressModel2
import com.vektor.vshare_api_ktx.model.EventModel
import java.io.Serializable

data class CustomerStatusModel (
    @SerializedName("status")
        val status: UserStatus,
    @SerializedName("user")
        val user: UserInfoModel,
    @SerializedName("vehicle")
        val vehicle: PoolCarVehicleModel,
    @SerializedName("upgradeAgreement")
        val upgradeAgreement: Boolean,
    @SerializedName("paymentRequired")
        val paymentRequired: Boolean,
    @SerializedName("balance")
        val balance: Double,
    @SerializedName("inBlackList")
        val inBlackList: Boolean,
    @SerializedName("rental")
        val rental: RentalModel?,
    @SerializedName("deliveryAddress")
        val deliveryAddress: DeliveryAddressModel2,
    @SerializedName("events")
        val events: List<EventModel>,
    @SerializedName("userDocumentInfo")
        val userDocumentInfo: UserDocumentInfo?,
    @SerializedName("flexirideRequestInfo")
        var flexirideRequestInfo: PoolcarAndFlexirideModel?
): Serializable

enum class UserStatus {
    @SerializedName("AVAILABLE")
    AVAILABLE,
    @SerializedName("ACTIVE")
    ACTIVE,
    @SerializedName("undefined")
    undefined
}

data class UserDocumentInfo(
        @SerializedName("drivingLicenseStatus")
        val drivingLicenseStatus: DrivingLicenseStatus,
        @SerializedName("anyDocumentPending")
        val anyDocumentPending: Boolean,
        @SerializedName("drivingLicenseRejectReason")
        val drivingLicenseRejectReason: String?

)

enum class DrivingLicenseStatus {
    @SerializedName("PENDING_APPROVAL")
    PENDING_APPROVAL,
    @SerializedName("MISSING")
    MISSING,
    @SerializedName("REJECTED")
    REJECTED
}