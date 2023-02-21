package com.vektortelekom.android.vservice.ui.login

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.vektor.ktx.data.remote.model.BaseErrorModel
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.LoginResponse
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.isValidEmail
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import retrofit2.HttpException
import javax.inject.Inject

class LoginViewModel
@Inject
constructor(private val userRepository: UserRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<LoginNavigator>() {


    val forgotPasswordResponse : MutableLiveData<BaseResponse> = MutableLiveData()
    val forgotPasswordEmail : MutableLiveData<String> = MutableLiveData()

    val loginResponse : MutableLiveData<LoginResponse> = MutableLiveData()
    val loginEmail : MutableLiveData<String> = MutableLiveData()
    val loginPassword : MutableLiveData<String> = MutableLiveData()
    var isCommuteOptionsEnabled: Boolean = false

    var langCode : String = "tr"

    val isRememberMe: MutableLiveData<Boolean> = MutableLiveData()

    private var isFirstLoginAttempt: Boolean = false

    fun forgotPassword(view: View?) {

        if(forgotPasswordEmail.value.isValidEmail()) {
            compositeDisposable.add(
                    userRepository.forgotPassword(forgotPasswordEmail.value?:"")
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                if(response.error != null) {
                                    navigator?.handleError(Exception(response.error?.message))
                                }
                                else {
                                    forgotPasswordResponse.value = response
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
    }

    fun register(view: View?) {
        navigator?.showRegisterActivity()
    }
    fun login(view: View?, isFirstTry: Boolean) {

        if(loginEmail.value.isValidEmail() && (loginPassword.value?.length ?: 0) > 3) {

            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
                continueToLogin(instanceIdResult.token, isFirstTry)
            }.addOnFailureListener {
                continueToLogin("", isFirstTry)
            }
        }
    }

    private fun continueToLogin(firebaseToken: String, isFirstTry: Boolean) {
        compositeDisposable.add(
                userRepository.login(loginEmail.value ?: "", loginPassword.value ?: "", firebaseToken, langCode)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response.error != null) {
                                AppLogger.d(response.error.toString())
                                if (response.error?.errorId == 192) {
                                    navigator?.tryLoginWithOtherServer(loginEmail.value ?: "", loginPassword.value ?: "", true)
                                }
                                else {
                                    navigator?.handleError(Exception(response.error?.message))
                                }
                            }
                            else {
                                getCompanySettings()
                                loginResponse.value = response

                            }
                        }, { ex ->
                            setIsLoading(false)
                            println("error: ${ex.localizedMessage}")
                            if (isFirstTry) {
                                when(ex) {
                                    is HttpException -> {
                                        var baseErrorModel: BaseErrorModel? = null
                                        try {
                                            val responseBody = ex.response()!!.errorBody()
                                            val gson = Gson()
                                            baseErrorModel = gson.fromJson(responseBody!!.string(), BaseErrorModel::class.java)
                                        } catch (t: Throwable) {
                                            AppLogger.e(t, "API Response Parse Error")
                                        }
                                        if (baseErrorModel?.error?.errorId == 192) {
                                            navigator?.tryLoginWithOtherServer(loginEmail.value ?: "", loginPassword.value ?: "", isFirstLoginAttempt)
                                        }
                                        else {
                                            navigator?.handleError(ex)
                                        }
                                        AppLogger.w("Error parse: ${baseErrorModel.toString()}")
                                    }
                                }
                            }
                            else {
                                navigator?.handleError(ex)
                            }

                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    private fun getCompanySettings() {
        compositeDisposable.add(
            userRepository.companySettings()
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    AppDataManager.instance.commuteOptionsEnabled = response.isCommuteOptionsEnabled ?: false
                    isCommuteOptionsEnabled = response.isCommuteOptionsEnabled ?: false
                }, { ex ->
                    println("error: ${ex.localizedMessage}")
                    navigator?.handleError(ex)
                }, {
                }, {
                }
                )
        )
    }

}