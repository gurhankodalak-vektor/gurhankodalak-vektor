package com.vektortelekom.android.vservice.ui.survey

import androidx.lifecycle.MutableLiveData
import com.vektortelekom.android.vservice.data.model.CommuteOptionsModel
import com.vektortelekom.android.vservice.data.model.SurveyAnswerRequest
import com.vektortelekom.android.vservice.data.model.SurveyQuestionResponse
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
        setCommuteOptions()
   /*     compositeDisposable.add(
                surveyRepository.getCommuteOptions()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response != null) {
                                // TODO: response geldikten sonra bu kısım düzenlenecektir. şuan için dummy data oluşturuyoruz. response alanı da değişecek
                                setCommuteOptions()
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
        )*/
    }
    private fun setCommuteOptions() {

        val shuttleText = CommuteOptionsModel(title = "shuttle", subtitle =  "20% subsidized", optionsButtonVisibility = true, cost = 15, costUnit = "USD", durationValue = 55, durationUnit = "min", emissionValue = 3.1, emissionUnit = "gr CO2")
        val transitText = CommuteOptionsModel(title = "Transit", subtitle =  "3 Options", optionsButtonVisibility = false, cost = 35, costUnit = "USD", durationValue = 53, durationUnit = "minute", emissionValue = 2.4, emissionUnit = "gr CO2")
        val drivingText = CommuteOptionsModel(title = "Driving", subtitle =  "", optionsButtonVisibility = false, cost = 67, costUnit = "USD", durationValue = 47, durationUnit = "minute", emissionValue = 5.2, emissionUnit = "gr CO2")

        optionsList.add(shuttleText)
        optionsList.add(transitText)
        optionsList.add(drivingText)

    }

}