package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ApprovalListModel : ArrayList<ApprovalListModelItem>()

data class ApprovalListModelItem(
        @SerializedName("id")
        val id: Long,
        @SerializedName("creationTime")
        val creationTime: Long,
        @SerializedName("isVanpoolUser")
        val isVanpoolUser: Boolean?,
        @SerializedName("isVanpoolDriver")
        val isVanpoolDriver: Boolean?,
        @SerializedName("routeId")
        val routeId: Int,
        @SerializedName("versionedRouteId")
        val versionedRouteId: Long,
        @SerializedName("workgroupInstanceId")
        val workgroupInstanceId: Long,
        @SerializedName("personnelId")
        val personnelId: Int,
        @SerializedName("responseDate")
        val responseDate: Long,
        @SerializedName("responseType")
        val responseType: String?,
        @SerializedName("approvalType")
        val approvalType: VanpoolApprovalType,
        @SerializedName("driverResponseStatus")
        val driverResponseStatus: VanpoolApprovalType?,
        @SerializedName("attandanceResponseStatus")
        val attandanceResponseStatus: VanpoolApprovalType?

) : Serializable

enum class VanpoolApprovalType(val type: String) : Serializable{
    VANPOOL_DRIVER("VANPOOL_DRIVER"),
    VANPOOL_USER("VANPOOL_USER"),
    DRIVER_AND_USER("DRIVER_AND_USER"),
    NONE("NONE")
}
