package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

data class VerifyEmailResponse (
        @SerializedName("personnel")
        val personnel: PersonnelModel,
        @SerializedName("sessionId")
        val sessionId: String,
        @SerializedName("surveyQuestionId")
        val surveyQuestionId: Int?,
) : BaseResponse(), Serializable