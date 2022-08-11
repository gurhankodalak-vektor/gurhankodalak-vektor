package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountingCompanyModel (
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("companyId")
        val companyId: Int,
        @SerializedName("yerleskeNo")
        val yerleskeNo: Int,
        @SerializedName("hasPayment")
        val hasPayment: Boolean,
        @SerializedName("mainAccount")
        val mainAccount: Boolean
): Serializable