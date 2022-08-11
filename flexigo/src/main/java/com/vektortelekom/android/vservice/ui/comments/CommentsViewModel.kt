package com.vektortelekom.android.vservice.ui.comments

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.TicketRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.convertForBackend
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import javax.inject.Inject

class CommentsViewModel
@Inject
constructor(
        private val ticketRepository: TicketRepository,
        private val userRepository: UserRepository,
        private val scheduler: SchedulerProvider) : BaseViewModel<CommentsNavigator>(){

    val tickets : MutableLiveData<List<TicketModel>> = MutableLiveData()
    val ticketTypes: MutableLiveData<List<TicketTypeModel>> = MutableLiveData()
    val destinations: MutableLiveData<List<DestinationModel>> = MutableLiveData()
    val routes: MutableLiveData<List<RouteModel>> = MutableLiveData()
    val description: MutableLiveData<String> = MutableLiveData()

    var selectedTicketType : TicketTypeModel? = null
    var selectedTicketTypeIndex : Int? = null

    var selectedDestination : DestinationModel? = null
    var selectedDestinationIndex : Int? = null

    var selectedRoute: RouteModel? = null
    var selectedRouteIndex: Int? = null

    var selectedDate: Date? = null

    var fileUuids : List<String>? = null

    var createTicketSuccess : MutableLiveData<Boolean> = MutableLiveData()

    var previewPhotoPath: String?= null

    fun getTickets(langCode: String? = "tr") {
        compositeDisposable.add(
                ticketRepository.getTickets(langCode)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            tickets.value = response.response
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

    fun getTicketTypes() {
        compositeDisposable.add(
                ticketRepository.getTicketTypes()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            ticketTypes.value = response.response
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

    fun getDestinations() {
        compositeDisposable.add(
                ticketRepository.getDestinations()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            destinations.value = response.response
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

    fun getDestinationRoutes(id: Long) {
        compositeDisposable.add(
                ticketRepository.getDestinationRoutes(id)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            routes.value = response.response
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

    fun submitComment(view: View?) {

        if(selectedTicketType == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_comment_select_demand_type)))
            }
        }
        else if(selectedDestination == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_comment_select_location_type)))
            }
        }
        else if(selectedRoute == null) {
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_comment_select_route_type)))
            }
        }
        else if(selectedDate == null){
            if (view != null) {
                navigator?.handleError(Exception(view.context.getString(R.string.warning_comment_select_date)))
            }
        }
        else {

            if(fileUuids.isNullOrEmpty()) {
                continueSubmitComment()
            }
            else {
                val parts = ArrayList<MultipartBody.Part>()
                for (i in fileUuids!!.indices) {
                    parts.add(MultipartBody.Part.createFormData("file" + (i + 1), "file" + (i + 1) + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), File(fileUuids!![i]))))
                }

                compositeDisposable.add(
                        userRepository.uploadImages(parts)
                                .observeOn(scheduler.ui())
                                .subscribeOn(scheduler.io())
                                .subscribe({ response ->
                                    fileUuids = response.response?.fileUuids
                                    continueSubmitComment()

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

        }

    }

    private fun continueSubmitComment() {

        val request = CreateTicketRequest(
                selectedTicketType?.key?:"",
                selectedDate.convertForBackend(),
                description.value?:"",
                selectedRoute?.id?:0,
                selectedRoute?.vehicle?.id?:0,
                        fileUuids?: listOf()
                )

        compositeDisposable.add(
                ticketRepository.createTicket(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response.error != null) {
                                navigator?.handleError(Exception(response.error?.message))
                            }
                            else {
                                selectedTicketType = null
                                selectedTicketTypeIndex = null
                                selectedRoute = null
                                selectedRouteIndex = null
                                selectedDate = null
                                selectedDestination = null
                                selectedDestinationIndex = null
                                description.value = null
                                fileUuids = null
                                createTicketSuccess.value = true
                            }
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

}