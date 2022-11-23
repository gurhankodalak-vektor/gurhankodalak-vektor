package com.vektortelekom.android.vservice.data.repository

import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.remote.service.CarPoolService
import javax.inject.Inject

class CarPoolRepository
@Inject
constructor(
        private val carPoolService: CarPoolService
) {

        fun getCarpool() = carPoolService.getCarpool()

        fun updateCarPoolPreferences(request: CarPoolPreferencesRequest) = carPoolService.updateCarpoolPreferences(request)

        fun setChooseDriver(request: ChooseDriverRequest) = carPoolService.setChooseDriver(request)

        fun setChooseRider(request: ChooseRiderRequest) = carPoolService.setChooseRider(request)

        fun getCountryCode() = carPoolService.getCountryCode()

        fun sendPhoneNumber(request: InfoUpdateRequest) = carPoolService.sendPhoneNumber(request)

        fun sendOtpCode(request: SendOtpRequest) = carPoolService.sendOtpCode(request)

        fun getMyQrCode() = carPoolService.getMyQrCode()

        fun getCarpoolUsage() = carPoolService.getCarpoolUsage()

        fun sendQrCode(value: ResponseModel) = carPoolService.sendQrCode(value)

}