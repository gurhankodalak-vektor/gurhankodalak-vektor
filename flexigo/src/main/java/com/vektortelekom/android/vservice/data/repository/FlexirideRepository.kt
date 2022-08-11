package com.vektortelekom.android.vservice.data.repository

import com.vektortelekom.android.vservice.data.model.PoolcarAndFlexirideModel
import com.vektortelekom.android.vservice.data.remote.service.FlexirideService
import javax.inject.Inject

class FlexirideRepository
@Inject
constructor(
        private val flexirideService: FlexirideService
) {

    fun createFlexiride(request: PoolcarAndFlexirideModel) = flexirideService.createFlexiride(request)

    fun getFlexirideList() = flexirideService.getFlexirideList()

    fun getFlexiride(id: Int) = flexirideService.getFlexiride(id)

    fun deleteFlexiride(id: Int) = flexirideService.deleteFlexiride(id)

}