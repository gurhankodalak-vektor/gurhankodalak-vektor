package com.vektortelekom.android.vservice.data.repository

import com.vektortelekom.android.vservice.data.model.SurveyAnswerRequest
import com.vektortelekom.android.vservice.data.remote.service.SurveyService
import javax.inject.Inject

class SurveyRepository
@Inject
constructor(
        private val surveyService: SurveyService
) {

    fun getSurveyQuestion(id: Int) = surveyService.getSurveyQuestion(id)

    fun uploadSurveyAnswers(surveyAnswerRequest: SurveyAnswerRequest) = surveyService.uploadSurveyAnswers(surveyAnswerRequest)

    fun getCommuteOptions() = surveyService.getCommuteOptions()

}