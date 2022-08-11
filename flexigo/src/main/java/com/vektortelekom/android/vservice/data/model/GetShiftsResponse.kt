package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse

data class GetShiftsResponse (
        @SerializedName("response")
        val response: List<ShiftModel>
): BaseResponse()