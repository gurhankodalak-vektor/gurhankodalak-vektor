package com.vektortelekom.android.vservice.ui.registration

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.iid.FirebaseInstanceId
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektortelekom.android.vservice.data.model.LoginResponse
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.isValidEmail
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import javax.inject.Inject

class RegistrationViewModel
@Inject
constructor(private val userRepository: UserRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<BaseNavigator>() {


    val isRememberMe: MutableLiveData<Boolean> = MutableLiveData()


}