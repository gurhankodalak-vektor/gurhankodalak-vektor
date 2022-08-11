package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CommuteOptionsModel (
        @SerializedName("title")
        val title: String,
        @SerializedName("subtitle")
        val subtitle: String,
        @SerializedName("optionsButtonVisibility")
        val optionsButtonVisibility: Boolean?,
        @SerializedName("cost")
        val cost: Int,
        @SerializedName("costUnit")
        val costUnit: String,
        @SerializedName("durationValue")
        val durationValue: Int,
        @SerializedName("durationUnit")
        val durationUnit: String,
        @SerializedName("emissionValue")
        val emissionValue: Double,
        @SerializedName("emissionUnit")
        val emissionUnit: String
) : Serializable