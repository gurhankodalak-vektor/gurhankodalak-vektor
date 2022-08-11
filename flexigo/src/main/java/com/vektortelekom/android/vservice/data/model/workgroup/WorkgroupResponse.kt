package com.vektortelekom.android.vservice.data.model.workgroup

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WorkgroupResponse(
        @SerializedName("instances")
        val instances: MutableList<WorkGroupInstance>,
        @SerializedName("templates")
        val templates: MutableList<WorkGroupTemplate>,
        @SerializedName("instance")
        val instance: WorkGroupInstance,
        @SerializedName("template")
        val template: WorkGroupTemplate
): Serializable