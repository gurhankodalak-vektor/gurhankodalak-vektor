package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class AgreeKvkkRequest (
        @SerializedName("isAggrement")
        val isAggrement: Boolean
)