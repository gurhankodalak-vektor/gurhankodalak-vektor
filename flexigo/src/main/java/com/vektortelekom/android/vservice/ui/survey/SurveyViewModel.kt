package com.vektortelekom.android.vservice.ui.survey

import androidx.lifecycle.MutableLiveData
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.SurveyRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class SurveyViewModel
@Inject
constructor(
            private val surveyRepository: SurveyRepository,
            private val scheduler: SchedulerProvider): BaseViewModel<SurveyNavigator>() {

    var questionId: MutableLiveData<Int?> = MutableLiveData()
    var isMultiSelectionEnabled: MutableLiveData<Boolean?> = MutableLiveData()
    var isContinueButtonEnabled: MutableLiveData<Boolean?> = MutableLiveData()
    var isSecondaryAnswerEnabled: MutableLiveData<Boolean?> = MutableLiveData()
    var isReloadFragment: MutableLiveData<Boolean?> = MutableLiveData()
    var surveyQuestion: MutableLiveData<SurveyQuestionResponse> = MutableLiveData()
    var selectedAnswers: MutableLiveData<List<Int>> = MutableLiveData()
    var secondaryAnswers: MutableLiveData<List<Int>> = MutableLiveData()
    var isSurveyFirstScreen: Boolean = false
    var optionsList : MutableList<CommuteOptionsModel> = ArrayList()
    var options: MutableLiveData<List<CommuteOptionsModel>> = MutableLiveData()

    var isLocationPermissionSuccess :  Boolean = false

    fun getSurveyQuestion(id: Int) {
        compositeDisposable.add(
                surveyRepository.getSurveyQuestion(id)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response != null) {
                                questionId.value = response.response.id
                                surveyQuestion.value = response.response
                                isMultiSelectionEnabled.value = response.response.multiSelectionEnabled!!
                                isContinueButtonEnabled.value = false
                                isSurveyFirstScreen = false
                                selectedAnswers.value = mutableListOf()
                                secondaryAnswers.value = mutableListOf()

                                navigator?.reloadFragment()

                                isReloadFragment.value = true

                            } else {
                                navigator?.handleError(Exception(response?.error?.message))
                            }
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)
                            navigator?.handleError(ex)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun uploadSurveyAnswers(surveyAnswerRequest: SurveyAnswerRequest) {
        compositeDisposable.add(
                surveyRepository.uploadSurveyAnswers(surveyAnswerRequest)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response != null) {
                                if (response.response.nextQuestionId != null){
                                    questionId.value = response.response.nextQuestionId
                                    getSurveyQuestion(questionId.value!!)
                                } else{
                                    questionId.value = null
                                    navigator?.showVanPoolLocationPermissionFragment()
                                }
                            } else {
                                navigator?.handleError(Exception(response?.error?.message))
                            }
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)
                            navigator?.handleError(ex)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun getCommuteOptions() {
        compositeDisposable.add(
                surveyRepository.getCommuteOptions()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response != null) {
                                setCommuteOptions(response.response!!)
                            } else {
                                navigator?.handleError(Exception(response?.error?.message))
                            }
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)
                            navigator?.handleError(ex)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }
    private fun setCommuteOptions(optionList: Response) {

        val shuttleText = CommuteOptionsModel(title = "Shuttle & Vanpool", subtitle =  "20% subsidized", optionsButtonVisibility = true, cost = optionList.personnelCommuteOptions?.commuteModeCost?.SHUTTLE, costUnit = "USD", durationValue = optionList.personnelCommuteOptions?.shuttleDurationInMin, durationUnit = "min", emissionValue = optionList.personnelCommuteOptions?.commuteModeEmission?.SHUTTLE, emissionUnit = "gr CO2")
        val transitText = CommuteOptionsModel(title = "Transit", subtitle =  (optionList.transitRoute.sections?.count() ?: 0).toString().plus(" Options"), optionsButtonVisibility = false, cost = optionList.personnelCommuteOptions?.commuteModeCost?.TRANSIT, costUnit = "USD", durationValue = optionList.personnelCommuteOptions?.publicDurationInMin, durationUnit = "minute", emissionValue = optionList.personnelCommuteOptions?.commuteModeEmission?.TRANSIT, emissionUnit = "gr CO2")
        val drivingText = CommuteOptionsModel(title = "Driving", subtitle =  "", optionsButtonVisibility = false, cost = optionList.personnelCommuteOptions?.commuteModeCost?.DRIVING, costUnit = "USD", optionList.personnelCommuteOptions?.carDurationInMin, durationUnit = "minute", emissionValue = optionList.personnelCommuteOptions?.commuteModeEmission?.DRIVING, emissionUnit = "gr CO2")


        optionsList.add(shuttleText)
        optionsList.add(transitText)
        optionsList.add(drivingText)

        options.value = optionsList

    }

}