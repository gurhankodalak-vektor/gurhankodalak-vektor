package com.vektortelekom.android.vservice.ui.notification

import androidx.lifecycle.MutableLiveData
import com.vektortelekom.android.vservice.data.model.NotificationModel
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class NotificationViewModel
@Inject
constructor(private val userRepository: UserRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<NotificationNavigator>() {

    val notificaitons: MutableLiveData<List<NotificationModel>> = MutableLiveData()

    fun getNotifications() {
        compositeDisposable.add(
                userRepository.getNotifications()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            notificaitons.value = response.response
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