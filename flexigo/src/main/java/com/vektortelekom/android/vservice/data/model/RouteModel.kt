package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupTemplate
import java.io.Serializable

data class RouteModel (
        @SerializedName("id")
        val id: Long,
        @SerializedName("driver")
        val driver: DriverModel,
        @SerializedName("destination")
        val destination: DestinationModel,
        @SerializedName("vehicle")
        val vehicle: VehicleModel,
        @SerializedName("serviceCompanyId")
        val serviceCompanyId: Int,
        @SerializedName("shift")
        val shift: ShiftModel,
        @SerializedName("accountingName")
        val accountingName: String?,
        @SerializedName("name")
        val name: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("totalKm")
        val totalKm: Double,
        @SerializedName("totalKmPlanned")
        val totalKmPlanned: Double?,
        @SerializedName("revisionPrice")
        val revisionPrice: Double?,
        @SerializedName("vehicleCapacity")
        val vehicleCapacity: Int,
        @SerializedName("personnelCount")
        val personnelCount: Int,
        @SerializedName("attendingPersonnelCount")
        val attendingPersonnelCount: Int,
        @SerializedName("reservationCount")
        val reservationCount: Int,
        @SerializedName("routeType")
        val routeType: String,
        @SerializedName("durationInMin")
        val durationInMin: Double?,
        @SerializedName("poiName")
        val poiName: String?,
        @SerializedName("companyId")
        val companyId: String?,
        @SerializedName("mapCoords")
        val mapCoords: String?,
        @SerializedName("routePath")
        val routePath: PathModel?,
        @SerializedName("returnRoutePath")
        val returnRoutePath: PathModel?,
        @SerializedName("vtspId")
        val vtspId: String?,
        @SerializedName("isActive")
        val isActive: Boolean,
        @SerializedName("passingBridge")
        val passingBridge: Boolean,
        @SerializedName("closeTime")
        val closeTime: String?,
        @SerializedName("routeCategory")
        val routeCategory: String?,
        @SerializedName("template")
        val template: WorkGroupTemplate?,

        var closestStation: StationModel? = null
) : Serializable{
        fun getRoutePath(isFirstLeg: Boolean): PathModel? {
                return if (isFirstLeg)
                        routePath!!
                else{
                        if (returnRoutePath?.data != null)
                                returnRoutePath
                        else
                                routePath

                }
        }
}
