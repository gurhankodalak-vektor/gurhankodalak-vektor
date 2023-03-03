package com.vektortelekom.android.vservice.ui.notification

import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.vektortelekom.android.vservice.data.model.CountryCodeResponseListModel
import com.vektortelekom.android.vservice.data.model.NotificationModel
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import io.reactivex.Observable
import javax.inject.Inject

class NotificationViewModel
@Inject
constructor(private val userRepository: UserRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<NotificationNavigator>() {

    val notifications: MutableLiveData<List<NotificationModel>> = MutableLiveData()

    var notificationListVisibility : MutableLiveData<Boolean> = MutableLiveData()

    fun getNotifications() {
        compositeDisposable.add(
                userRepository.getNotifications()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if (response != null && response.isNotEmpty()) {
                                notifications.value = response
                                notificationListVisibility.value = true
                            } else {
                                notificationListVisibility.value = false
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