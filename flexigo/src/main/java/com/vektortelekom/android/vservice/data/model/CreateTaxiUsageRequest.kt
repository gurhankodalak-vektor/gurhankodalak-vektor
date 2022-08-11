package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

data class CreateTaxiUsageRequest(
        @SerializedName("id")
        val id: Int? = null,
        @SerializedName("startLocation")
        val startLocation: TaxiLocationModel?,
        @SerializedName("endLocation")
        var endLocation: TaxiLocationModel? = null,
        @SerializedName("purposeOfUse")
        var purposeOfUse: String? = null,
        @SerializedName("explanation")
        var explanation: String? = null,
        @SerializedName("declaredTaxiFare")
        var declaredTaxiFare: Double? = null,
        @SerializedName("usageDate")
        var usageDate: String?,
        @SerializedName("fileUuids")
        var fileUuids: List<String>? = null
) : Serializable, BaseResponse()
/*
enum class PurposeType {
    WORK,
    MEETING
}*/

data class TaxiLocationModel(
        @SerializedName("latitude")
        val latitude: Double,
        @SerializedName("longitude")
        val longitude: Double,
        @SerializedName("address")
        val address: String,
        @SerializedName("externalReferenceId")
        var externalReferenceId: String? = null,
        @SerializedName("name")
        val name: String? = null
): Serializable

data class CreateTaxiResponse(
        @SerializedName("response")
        val response: CreateTaxiUsageRequest
): Serializable, BaseResponse()