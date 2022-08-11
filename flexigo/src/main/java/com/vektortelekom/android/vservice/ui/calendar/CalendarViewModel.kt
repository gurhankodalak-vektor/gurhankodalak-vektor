package com.vektortelekom.android.vservice.ui.calendar

import androidx.lifecycle.MutableLiveData
import com.vektortelekom.android.vservice.data.model.SendGoogleAuthCodeRequest
import com.vektortelekom.android.vservice.data.model.ShuttleDayModel
import com.vektortelekom.android.vservice.data.repository.CalendarRepository
import com.vektortelekom.android.vservice.data.repository.ShuttleRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.convertForBackend
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import java.util.*
import javax.inject.Inject

class CalendarViewModel
@Inject
constructor(private val userRepository: UserRepository,
            private val shuttleRepository: ShuttleRepository,
            private val calendarRepository: CalendarRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<CalendarNavigator>() {

    val shuttleUseDays: MutableLiveData<List<ShuttleDayModel>> = MutableLiveData()

    val googleCalendarEmail: MutableLiveData<String?> = MutableLiveData()
    val googleCalendarAccessToken: MutableLiveData<String?> = MutableLiveData()

    val outlookCalendarEmail: MutableLiveData<String?> = MutableLiveData()
    val outlookCalendarAccessToken: MutableLiveData<String?> = MutableLiveData()

    fun getShuttleUseDays(date: Date) {
        compositeDisposable.add(
                shuttleRepository.getShuttleUseDays(date.convertForBackend(), date.convertForBackend())
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response.error != null) {
                                navigator?.handleError(Exception(response.error?.message))
                            }
                            else {
                                shuttleUseDays.value = response.response
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

    fun sendGoogleAuthCode(authCode: String) {

        val request = SendGoogleAuthCodeRequest(authCode)

        compositeDisposable.add(
                calendarRepository.sendGoogleAuthCode(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({

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