package com.vektortelekom.android.vservice.data.repository

import com.vektor.vshare_api_ktx.model.*
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.remote.service.PoolCarService
import okhttp3.MultipartBody
import javax.inject.Inject

class PoolCarRepository
@Inject
constructor(
        private val poolCarService: PoolCarService
)
{

    fun getStations() = poolCarService.getStations()

    fun getStationVehicles(id: Int) = poolCarService.getStationVehicles(id)

    fun getCustomerStatus() = poolCarService.getCustomerStatus()

    fun createRental(request: RentalCreateRequest2) = poolCarService.createRental(request)

    fun cancelRental(request: RentalEndRequest) = poolCarService.cancelRental(request)

    fun startRental(request: StartRentalRequest) = poolCarService.startRental(request)

    fun getVehicleDamages(vehicleId: Int) = poolCarService.getVehicleDamages(vehicleId)

    fun getRentalDamages(rentalId: Int) = poolCarService.getRentalDamages(rentalId)

    fun rentalEnd(request: RentalEndRequest) = poolCarService.rentalEnd(request)

    fun uploadImages(files: List<MultipartBody.Part>) = poolCarService.uploadImages2(files)

    fun addCarDamage(request: DamageRequest) = poolCarService.addCarDamages(request)

    fun checkDoorStatus(rentalId: Int) = poolCarService.checkDoorStatus(rentalId)

    fun getMobileParameters() = poolCarService.getMobileParameters()

    fun getReservations() = poolCarService.getReservations()

    fun getReservationReasons() = poolCarService.getReservationReasons()

    fun addReservation(request: PoolcarAndFlexirideModel) = poolCarService.addReservation(request)

    fun checkReservation(request: PoolcarAndFlexirideModel) = poolCarService.checkReservation(request)

    fun cancelReservation(id: Int) = poolCarService.cancelReservation(id)

    fun getPoiList(poiRequest: PoiRequest) = poolCarService.getPoiList(poiRequest)

    fun getRentalBillInfo(rentalId: Long) = poolCarService.getRentalBillInfo(rentalId)

    fun availablePriceModels(request: PoolcarAndFlexirideModel) = poolCarService.availablePriceModels(request)

    fun getPoiList() = poolCarService.getPoiList()

    fun startIntercityRental(id: Int, request: PoolCarIntercityStartRequest) = poolCarService.startIntercityRental(id, request)

    fun finishIntercityRental(id: Int, request: PoolCarIntercityFinishRequest) = poolCarService.finishIntercityRental(id, request)

    fun updateReservationVehicleWithQr(id: Int, qrCode: String) = poolCarService.updateReservationVehicleWithQr(id, UpdateReservationVehicleRequest(qrCode))

}