package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class SearchPersonRequest (
        @SerializedName("identityNumber")
        val identityNumber: String
)