package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DepartmentModel(
        @SerializedName("id")
        val id: Int,
        @SerializedName("companyId")
        val companyId: Int,
        @SerializedName("yerleskeNo")
        val yerleskeNo: Int,
        @SerializedName("yerleske")
        val yerleske: String,
        @SerializedName("kurumNo")
        val kurumNo: Int,
        @SerializedName("kurumAdi")
        val kurumAdi: String,
        @SerializedName("uniteNo")
        val uniteNo: Int,
        @SerializedName("uniteAdi")
        val uniteAdi: String,
        @SerializedName("isFree")
        val isFree: Boolean,
        @SerializedName("accountingCompanyId")
        val accountingCompanyId: Int
) : Serializable