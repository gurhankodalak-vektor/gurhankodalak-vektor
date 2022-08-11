package com.vektortelekom.android.vservice.data.repository

import com.vektortelekom.android.vservice.data.model.SendGoogleAuthCodeRequest
import com.vektortelekom.android.vservice.data.remote.service.CalendarService
import javax.inject.Inject

class CalendarRepository
@Inject
constructor(
        private val calendarService: CalendarService
) {

    fun sendGoogleAuthCode(request: SendGoogleAuthCodeRequest) = calendarService.sendGoogleAuthCode(request)

}