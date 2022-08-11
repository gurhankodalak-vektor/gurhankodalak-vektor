package com.vektortelekom.android.vservice.data.repository

import com.vektortelekom.android.vservice.data.model.CreateTaxiUsageRequest
import com.vektortelekom.android.vservice.data.remote.service.TaxiService
import javax.inject.Inject

class TaxiRepository
@Inject
constructor(
        private val taxiService: TaxiService
) {

    fun createTaxiUsage(request: CreateTaxiUsageRequest) = taxiService.createTaxiUsage(request)

    fun updateTaxiUsage(id: Int, request: CreateTaxiUsageRequest) = taxiService.updateTaxiUsage(id, request)

    fun getTaxiUsages() = taxiService.getTaxiUsages()

}