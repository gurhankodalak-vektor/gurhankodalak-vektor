package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TaxiUsage (
        @SerializedName("id")
        val id: Int,
        @SerializedName("creationTime")
        val creationTime: Long,
        @SerializedName("startLocation")
        val startLocation: TaxiLocationModel,
        @SerializedName("endLocation")
        val endLocation: TaxiLocationModel,
        @SerializedName("purposeOfUse")
        val purposeOfUse: String,
        @SerializedName("explanation")
        val explanation: String,
        @SerializedName("declaredTaxiFare")
        val declaredTaxiFare: Double,
        @SerializedName("calculatedAmount")
        val calculatedAmount: Double,
        @SerializedName("paymentAmount")
        val paymentAmount: Double,
        @SerializedName("status")
        val status: String
): Serializable