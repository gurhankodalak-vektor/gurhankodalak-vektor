package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

data class SurveyAnswerResponse(
        @SerializedName("response")
        val response: SurveyAnswerResponseBase
) : BaseResponse(), Serializable

data class SurveyAnswerResponseBase(
        @SerializedName("nextQuestionId")
        val nextQuestionId: Int?

): Serializable