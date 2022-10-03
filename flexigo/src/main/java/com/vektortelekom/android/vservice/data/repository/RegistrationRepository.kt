package com.vektortelekom.android.vservice.data.repository

import com.vektortelekom.android.vservice.data.remote.service.RegistrationService
import io.reactivex.Observable
import javax.inject.Inject

class RegistrationRepository
@Inject
constructor(
        private val registrationService: RegistrationService
) {


}