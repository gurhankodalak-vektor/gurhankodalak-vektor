package com.vektortelekom.android.vservice.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CompanySettingsResponse(
    @SerializedName("checkBlacklist")
    val checkBlacklist: Boolean,
    @SerializedName("companyId")
    val companyId: Int,
    @SerializedName("companyTitle")
    val companyTitle: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("dateFormat")
    val dateFormat: String,
    @SerializedName("driverVehicleAssignApprovalRequired")
    val driverVehicleAssignApprovalRequired: Boolean,
    @SerializedName("isCommuteOptionsEnabled")
    val isCommuteOptionsEnabled: Boolean?,
    @SerializedName("isPoolCarEnabled")
    val isPoolCarEnabled: Boolean,
    @SerializedName("driversCanBeCalled")
    val driversCanBeCalled: Boolean,
    @SerializedName("isTaxiEnabled")
    val isTaxiEnabled: Boolean,
    @SerializedName("lengthUnit")
    val lengthUnit: String,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("weightUnit")
    val weightUnit: String
): Serializable