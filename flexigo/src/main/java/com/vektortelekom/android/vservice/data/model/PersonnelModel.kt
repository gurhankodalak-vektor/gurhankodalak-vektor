package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PersonnelModel (
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("surname")
        val surname: String,
        @SerializedName("phoneNumber")
        var phoneNumber: String,
        @SerializedName("email")
        val email: String,
        @SerializedName("identityNumber")
        val identityNumber: String,
        @SerializedName("isActive")
        val isActive: Boolean?,
        @SerializedName("departmentId")
        val departmentId: Int,
        @SerializedName("validUntil")
        val validUntil: Long,
        @SerializedName("isUsingService")
        val isUsingService: Boolean,
        @SerializedName("homeLocation")
        var homeLocation: LocationModel?,
        @SerializedName("companyId")
        val companyId: Int,
        @SerializedName("destinationId")
        val destinationId: Int,
        @SerializedName("routeId")
        val routeId: Long,
        @SerializedName("status")
        val status: String,
        @SerializedName("fullName")
        val fullName: String,
        @SerializedName("pragnent")
        val pragnent: Boolean,
        @SerializedName("station")
        val station: StationModel?,
        @SerializedName("destination")
        val destination: DestinationModel?,
        @SerializedName("company")
        val company: CompanyModel?,
        @SerializedName("profileImageUuid")
        var profileImageUuid: String?,
        @SerializedName("walkingDistanceInMeter")
        val walkingDistanceInMeter: String?,
        @SerializedName("isHandicapped")
        val isHandicapped: Boolean?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("cardId")
        val cardId: String?,
        @SerializedName("liveTaxiUse")
        val liveTaxiUse : CreateTaxiUsageRequest?,
        @SerializedName("accountId")
        val accountId: Int?,
        @SerializedName("aggrementDate")
        val aggrementDate: Long?,
        @SerializedName("surveyQuestionId")
        val surveyQuestionId: Int?,
        @SerializedName("workgroupInstanceId")
        val workgroupInstanceId: Long?
) : Serializable