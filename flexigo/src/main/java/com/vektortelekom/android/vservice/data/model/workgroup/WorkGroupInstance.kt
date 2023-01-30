package com.vektortelekom.android.vservice.data.model.workgroup

import com.google.gson.annotations.SerializedName
import com.vektortelekom.android.vservice.data.model.WorkgroupStatus
import java.io.Serializable

data class WorkGroupInstance(
    @SerializedName("id")
    val id: Long,
    @SerializedName("companyId")
    val companyId: Long?,
    @SerializedName("endDate")
    val endDate: Long?,
    @SerializedName("firstDepartureDate")
    var firstDepartureDate: Long?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("startDate")
    val startDate: Long?,
    @SerializedName("templateId")
    val templateId: Long?,
    @SerializedName("demandStartDeadline")
    val demandStartDeadline: Long?,
    @SerializedName("demandDeadline")
    val demandDeadline: Long?,
    @SerializedName("workgroupStatus")
    val workgroupStatus: WorkgroupStatus
): Serializable