package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CompanyModel (
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("companyGroupId")
        val companyGroupId: Int?,
        @SerializedName("vtspDn")
        val vtspDn: String,
        @SerializedName("termsOfUseUrl")
        val termsOfUseUrl: String?,
        @SerializedName("destinations")
        val destinations: List<DestinationModel>,
        @SerializedName("regions")
        val regions: List<String>,
        @SerializedName("companySettings")
        val companySettings: String?,
        @SerializedName("accounts")
        val accounts: List<String>,
        @SerializedName("serviceCompanies")
        val serviceCompanies: List<String>,
        @SerializedName("destinationCount")
        val destinationCount: Int?,
        @SerializedName("routeCount")
        val routeCount: Int?,
        @SerializedName("personnelCount")
        val personnelCount: Int?,
        @SerializedName("noRouteCount")
        val noRouteCount: Int?,
        @SerializedName("mobileUser")
        val mobileUser: String?,
        @SerializedName("adminAccount")
        val adminAccount: String?,
        @SerializedName("overallTicketCount")
        val overallTicketCount: Int?,
        @SerializedName("isActive")
        val isActive: Boolean?,
        @SerializedName("personnelUsingShuttle")
        val personnelUsingShuttle: String?,
        @SerializedName("personnelNotUsingShuttle")
        val personnelNotUsingShuttle: String?,
        @SerializedName("kvkkDocUrl")
        val kvkkDocUrl: String?,
        @SerializedName("securityDocUrl")
        val securityDocUrl: String?,
        @SerializedName("rulesDocUrl")
        val rulesDocUrl: String?
): Serializable