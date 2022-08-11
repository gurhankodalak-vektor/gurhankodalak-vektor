package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import java.io.Serializable

data class SurveyQuestionModel(
        @SerializedName("response")
        val response: SurveyQuestionResponse
) : BaseResponse(), Serializable

data class SurveyQuestionResponse(
        @SerializedName("id")
        val id: Int,
        @SerializedName("surveyType")
        val surveyType: String,
        @SerializedName("multiSelectionEnabled")
        val multiSelectionEnabled: Boolean?,
        @SerializedName("isCurrentMode")
        val isCurrentMode: Boolean?,
        @SerializedName("questionText")
        val questionText: String?,
        @SerializedName("description")
        val description: String?,
        @SerializedName("answers")
        val answers: List<SurveyQuestionAnswerModel>,
        @SerializedName("secondaryDescription")
        val secondaryDescription: String?

): Serializable

data class SurveyQuestionAnswerModel(
        @SerializedName("id")
        val id: Int,
        @SerializedName("questionId")
        val questionId: Int?,
        @SerializedName("answerOrder")
        val answerOrder: Int?,
        @SerializedName("answerText")
        val answerText: String?,
        @SerializedName("nextQuestionId")
        val nextQuestionId: Int?

): Serializable