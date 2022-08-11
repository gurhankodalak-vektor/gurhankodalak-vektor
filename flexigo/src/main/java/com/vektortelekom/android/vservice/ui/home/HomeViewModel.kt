package com.vektortelekom.android.vservice.ui.home

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.model.workgroup.WorkgroupResponse
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import retrofit2.HttpException
import javax.inject.Inject

class HomeViewModel
@Inject
constructor(private val userRepository: UserRepository,
            private val poolCarRepository: PoolCarRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<HomeNavigator>(){

    val dashboardResponse: MutableLiveData<DashboardResponse> = MutableLiveData()

    val name: MutableLiveData<String> = MutableLiveData()

    var isPoolCarActive: Boolean = false

    var countPoolCarVehicle: MutableLiveData<Int?> = MutableLiveData()

    var taxiUsage: MutableLiveData<CreateTaxiUsageRequest?> = MutableLiveData()

    val customerStatus: MutableLiveData<CustomerStatusModel> = MutableLiveData()

    val agreeKvkkResponse: MutableLiveData<Boolean> = MutableLiveData()
    val isForDrivingLicence: MutableLiveData<Boolean> = MutableLiveData()

    val approvalListItem: MutableLiveData<ApprovalListModelItem> = MutableLiveData()
    val campusInfo: MutableLiveData<CampusesModel> = MutableLiveData()
    val isUpdateSuccess: MutableLiveData<Boolean> = MutableLiveData()

    val draftRouteDetails: MutableLiveData<RouteDraftsModel> = MutableLiveData()

    val workgroupInfo: MutableLiveData<WorkgroupResponse> = MutableLiveData()
    val instanceId: MutableLiveData<Long> = MutableLiveData()
    val versionedRouteId: MutableLiveData<Long> = MutableLiveData()
    val approvalItemId: MutableLiveData<Long> = MutableLiveData()
    val approvalType: MutableLiveData<VanpoolApprovalType> = MutableLiveData()

    val textviewVanpoolRouteName: MutableLiveData<String> = MutableLiveData()
    val textviewVanpoolStationName: MutableLiveData<String> = MutableLiveData()
    val textviewVanpoolWalkingDistance: MutableLiveData<String> = MutableLiveData()
    val textviewVanpoolDepartureFromStop: MutableLiveData<String> = MutableLiveData()
    val textviewVanpoolDepartureFromCampus: MutableLiveData<String> = MutableLiveData()

    var myLocation: Location? = null

    fun getVanpoolApprovalList() {

        compositeDisposable.add(
                userRepository.getVanpoolApprovalList()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            approvalListItem.value = response.first()
                            instanceId.value = response.first().workgroupInstanceId
                            versionedRouteId.value = response.first().versionedRouteId
                            approvalType.value = response.first().approvalType
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun getDraftRouteDetails(routeId: Long) {

        compositeDisposable.add(
                userRepository.getDraftRouteDetails(routeId)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            draftRouteDetails.value = response
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

    fun getWorkgroupInformation(workgroupInstanceId: Long) {
        compositeDisposable.add(
                userRepository.getWorkgroupInformation(workgroupInstanceId)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            workgroupInfo.value = response
                        }, { ex ->
                            setIsLoading(false)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }
    fun getCampusInfo(campusId: Long) {
        compositeDisposable.add(
                userRepository.getCampusInfo(campusId)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            campusInfo.value = response
                        }, { ex ->
                            setIsLoading(false)
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun updateApproval(approvalItemId: Long, responseType: String) {
        compositeDisposable.add(
                userRepository.updateResponse(approvalItemId, responseType)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({
                            isUpdateSuccess.value = true
                        }, { ex ->
                            setIsLoading(false)
                            isUpdateSuccess.value = true
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun getDashboard(langCode: String? = "tr") {

        compositeDisposable.add(
                userRepository.getDashboard(langCode)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            dashboardResponse.value = response
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

    fun getNotifications() {

        compositeDisposable.add(
                userRepository.getNotifications()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            //dashboardResponse.value = response
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

    fun getStations() {

        compositeDisposable.add(
                poolCarRepository.getStations()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            var count = 0

                            for(station in response) {
                                count += station.vehicleAvailableCount?:0
                            }

                            countPoolCarVehicle.value = count

                            isPoolCarActive = true

                            getCustomerStatus()

                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)

                            isPoolCarActive = false

                            when (ex) {
                                is HttpException -> {
                                    if (ex.response()?.code() == 403) {
                                        sessionExpireError.value = true
                                    } else {
                                        navigator?.handleError(ex)
                                    }
                                }
                                else -> {
                                    navigator?.handleError(ex)
                                }
                            }
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun getPersonnelInfo() {
        compositeDisposable.add(
                userRepository.getPersonalDetails()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            taxiUsage.value = response.response.liveTaxiUse
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

    fun getCustomerStatus() {

        compositeDisposable.add(
                poolCarRepository.getCustomerStatus()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            AppDataManager.instance.carShareUser = response
                            customerStatus.value = response
                        }, { ex ->
                            println("error: ${ex.localizedMessage}")
                            setIsLoading(false)
                            when (ex) {
                                is HttpException -> {
                                    if (ex.response()?.code() == 403) {
                                    } else {
                                        navigator?.handleError(ex)
                                    }
                                }
                                else -> {
                                    navigator?.handleError(ex)
                                }
                            }
                        }, {
                            setIsLoading(false)
                        }, {
                            setIsLoading(true)
                        }
                        )
        )
    }

    fun agreeKvkk() {
        compositeDisposable.add(
                userRepository.agreeKvkk(AgreeKvkkRequest(true))
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            agreeKvkkResponse.value = true
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