package com.vektortelekom.android.vservice.data.repository

import com.vektortelekom.android.vservice.data.model.CarPoolPreferencesRequest
import com.vektortelekom.android.vservice.data.model.ChooseDriverRequest
import com.vektortelekom.android.vservice.data.model.ChooseRiderRequest
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

}