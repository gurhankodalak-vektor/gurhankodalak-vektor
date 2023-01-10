package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

data class CommuteOptionsResponse(
    @SerializedName("response")
    val response: Response?
): BaseResponse(), Serializable

data class Response(
    @SerializedName("personnelCommuteOptions")
    val personnelCommuteOptions: PersonnelCommuteOptions?,
    @SerializedName("transitRoute")
    val transitRoute: TransitRoute?,
    @SerializedName("transportStats")
    val transportStats: TransportStats
): Serializable