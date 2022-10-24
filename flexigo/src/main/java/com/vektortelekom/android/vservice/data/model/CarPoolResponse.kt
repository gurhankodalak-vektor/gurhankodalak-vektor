package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

class CarPoolResponse (
        @SerializedName("response")
        val response: CarPoolModel
) : Serializable, BaseResponse()