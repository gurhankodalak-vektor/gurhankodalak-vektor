package com.vektortelekom.android.vservice.data.remote.service

import com.vektortelekom.android.vservice.data.model.SurveyAnswerRequest
import com.vektortelekom.android.vservice.data.model.SurveyAnswerResponse
import com.vektortelekom.android.vservice.data.model.SurveyQuestionModel
import io.reactivex.Observable
import retrofit2.http.*

interface SurveyService {

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/personnel/survey/question/{id}")
    fun getSurveyQuestion(@Path("id") id: Int): Observable<SurveyQuestionModel>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @POST("/{app_name}/rest/mobile/personnel/survey/answer")
    fun uploadSurveyAnswers(@Body surveyAnswerRequest: SurveyAnswerRequest): Observable<SurveyAnswerResponse>

    @Headers(
            "Content-Type: application/json",
            "Accept: application/json"
    )
    @GET("/{app_name}/rest/mobile/personnel/workgroup/commute-options")
    fun getCommuteOptions(): Observable<SurveyAnswerResponse>

}