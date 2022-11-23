package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChooseDriverRequest (
        @SerializedName("driverPersonnelId")
        val driverPersonnelId: Long?,
        @SerializedName("isMatchedState")
        val isMatchedState: Boolean?
) : Serializable