package com.vektortelekom.android.vservice.ui.registration

import androidx.lifecycle.MutableLiveData
import com.vektortelekom.android.vservice.data.repository.RegistrationRepository
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class RegistrationViewModel
@Inject
constructor(private val registrationRepository: RegistrationRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<BaseNavigator>() {


    val isRememberMe: MutableLiveData<Boolean> = MutableLiveData()


}