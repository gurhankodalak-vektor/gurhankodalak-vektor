package com.vektortelekom.android.vservice.data.model.workgroup

import com.google.gson.annotations.SerializedName
import com.vektortelekom.android.vservice.data.model.FromToType
import com.vektortelekom.android.vservice.data.model.WorkgroupDirection
import java.io.Serializable

data class WorkGroupTemplate(
        @SerializedName("companyId")
        val companyId: Long?,
        @SerializedName("description")
        val description: String?,
        @SerializedName("direction")
        val direction: WorkgroupDirection?,
        @SerializedName("fromTerminalReferenceId")
        val fromTerminalReferenceId: Long?,
        @SerializedName("fromType")
        val fromType: FromToType?,
        @SerializedName("id")
        val id: Long,
        @SerializedName("name")
        val name: String?,
        @SerializedName("workgroupType")
        val workgroupType: String?,
        @SerializedName("shift")
        val shift: WorkGroupShift?,
        @SerializedName("demandStartDeadline")
        val demandStartDeadline: DemandDeadline?,
        @SerializedName("demandDeadline")
        val demandDeadline: DemandDeadline?,
        @SerializedName("toTerminalReferenceId")
        val toTerminalReferenceId: Long?,
        @SerializedName("toType")
        val toType: FromToType?
): Serializable

data class DemandDeadline(
        @SerializedName("deadlineType")
        val deadlineType: String?,
        @SerializedName("deadlineDate")
        val deadlineDate: Long?,
        @SerializedName("deadlineHours")
        val deadlineHours: Int?
): Serializable