package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SurveyAnswerRequest (
        @SerializedName("questionId")
        val questionId: Int,
        @SerializedName("answerIds")
        val answerIds: List<Int>,
        @SerializedName("secondaryModeAnswerIds")
        val secondaryModeAnswerIds: List<Int>
): Serializable