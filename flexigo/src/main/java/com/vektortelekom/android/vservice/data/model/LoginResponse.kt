package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

data class LoginResponse(
        @SerializedName("personnel")
        val personnel: PersonnelModel,
        @SerializedName("sessionId")
        val sessionId: String,
        @SerializedName("route")
        val route: RouteModel,
        @SerializedName("company")
        val company: CompanyModel,
        @SerializedName("destination")
        val destination: DestinationModel,
        @SerializedName("shift")
        val shift: ShiftModel?,
        @SerializedName("routeInfo")
        val routeInfo: List<RouteInfoModel>,
        @SerializedName("vtspSessionId")
        val vtspSessionId: String,
        @SerializedName("changePasswordRequired")
        val changePasswordRequired: Boolean,
        @SerializedName("department")
        val department: DepartmentModel,
        @SerializedName("stations")
        val stations: List<StationModel>,
        @SerializedName("vehicle")
        val vehicle: VehicleModel?,
        @SerializedName("personnels")
        val personnels: String?,
        @SerializedName("accountingCompany")
        val accountingCompany: AccountingCompanyModel,
        @SerializedName("surveyQuestionId")
        val surveyQuestionId: Int?,
        @SerializedName("workgroupInstanceId")
        val workgroupInstanceId: Long?
): BaseResponse(), Serializable