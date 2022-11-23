package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChooseRiderRequest (
        @SerializedName("riderPersonnelId")
        val riderPersonnelId: Long?,
        @SerializedName("isMatchedState")
        val isMatchedState: Boolean?,
        @SerializedName("driverApproved")
        val driverApproved: Boolean?
) : Serializable