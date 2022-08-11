package com.vektortelekom.android.vservice.ui.poi.gasstation

import androidx.lifecycle.MutableLiveData
import com.vektor.ktx.utils.logger.AppLogger
import com.vektor.vshare_api_ktx.model.PoiRequest
import com.vektor.vshare_api_ktx.model.PoiResponse
import com.vektortelekom.android.vservice.data.model.ParkModel
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.ui.base.BaseNavigator
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import retrofit2.HttpException
import javax.inject.Inject


class GasStationViewModel
@Inject
constructor(private val poolCarRepository: PoolCarRepository, private val scheduler: SchedulerProvider) : BaseViewModel<BaseNavigator>() {

    val poiList: MutableLiveData<List<PoiResponse>> = MutableLiveData()

    val stations : MutableLiveData<List<ParkModel>> = MutableLiveData()

    fun getPoiList(poiRequest: PoiRequest) {
        compositeDisposable.add(
                poolCarRepository.getPoiList(poiRequest)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            poiList.value = response
                        }, { ex ->
                            AppLogger.e(ex, "operation failed.")
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

                            stations.value = response

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

}