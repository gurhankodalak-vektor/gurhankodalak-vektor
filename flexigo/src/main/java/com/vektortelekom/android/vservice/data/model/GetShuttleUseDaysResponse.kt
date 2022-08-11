package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

data class GetShuttleUseDaysResponse (
        @SerializedName("response")
        val response : List<ShuttleDayModel>
) : BaseResponse(), Serializable