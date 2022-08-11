package com.vektortelekom.android.vservice.ui.taxi

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.repository.TaxiRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.taxi.dialog.TaxiCreateDialog
import com.vektortelekom.android.vservice.utils.convertForBackend2
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class TaxiViewModel
@Inject
constructor(
        private val taxiRepository: TaxiRepository,
        private val userRepository: UserRepository,
        private val scheduler: SchedulerProvider): BaseViewModel<TaxiNavigator>() {

    var selectedDateReport: Date? = null
    var selectedDateTextReport: String? = null
    var selectedDateStart: Date? = null
    var selectedDateTextStart: String? = null
    var selectedDateFinish: Date? = null
    var selectedDateTextFinish: String? = null

    val startLocationReport : MutableLiveData<LatLng> = MutableLiveData()
    val endLocationReport : MutableLiveData<LatLng> = MutableLiveData()

    val endLocationFinish : MutableLiveData<LatLng> = MutableLiveData()

    val startLocationStart : MutableLiveData<LatLng> = MutableLiveData()
    //val endLocationStart : MutableLiveData<LatLng> = MutableLiveData()

    val startLocationTextReport: MutableLiveData<String> = MutableLiveData()
    val endLocationTextReport: MutableLiveData<String> = MutableLiveData()

    val endLocationTextFinish: MutableLiveData<String> = MutableLiveData()

    val startLocationTextStart: MutableLiveData<String> = MutableLiveData()
    //val endLocationTextStart: MutableLiveData<String> = MutableLiveData()

    val descriptionReport: MutableLiveData<String> = MutableLiveData()
    val payAmountReport: MutableLiveData<String> = MutableLiveData()

    val descriptionFinish: MutableLiveData<String> = MutableLiveData()
    val payAmountFinish: MutableLiveData<String> = MutableLiveData()

    var selectedPurposeIndexReport: Int? = null
    var selectedPurposeTypeReport : VektorEnum? = null

    var selectedPurposeIndexFinish: Int? = null
    var selectedPurposeTypeFinish : VektorEnum? = null

    var reportPhotoUrl: String? = null

    var finishPhotoUrl: String? = null

    private var fileUuidsReport: List<String>? = null

    private var fileUuidsFinish: List<String>? = null

    val taxiUsages: MutableLiveData<List<TaxiUsage>> = MutableLiveData()

    val taxiUsageCreated: MutableLiveData<Boolean> = MutableLiveData()

    val taxiUsageStarted: MutableLiveData<Boolean> = MutableLiveData()
    val taxiUsageFinished: MutableLiveData<Boolean> = MutableLiveData()

    val purposes: MutableLiveData<List<VektorEnum>> = MutableLiveData()

    var isStart: Boolean = true
    var isReport: Int = 1

    var taxiUsage: MutableLiveData<CreateTaxiUsageRequest?> = MutableLiveData()

    var isStartLocationChangedManuelly = false
    var isEndLocationChangedManuelly = false

    fun reportTaxiUsage(view: View?) {

        if(selectedDateReport == null
                || startLocationReport.value == null
                || endLocationReport.value == null
                || descriptionReport.value == null
                || selectedPurposeTypeReport == null
                || (if (selectedPurposeTypeReport?.value == "PERSONAL") false else
                        (payAmountReport.value == null
                        || reportPhotoUrl == null ))
                ) {

            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.error_password_empty)))
            }
            return
        }

        view?.let {
            TaxiCreateDialog.Builder(it.context)
                    .setTitle(it.context.getString(R.string.report_taxi_use))
                    .setTextDate(selectedDateTextReport!!)
                    .setTextStart(startLocationTextReport.value?:"")
                    .setTextFinish(endLocationTextReport.value?:"")
                    .setTextDescription(descriptionReport.value!!)
                    .setTextPay(payAmountReport.value?:"")
                    .setOkButton { dialog ->
                        dialog.dismiss()

                        if(reportPhotoUrl != null) {
                            val parts = ArrayList<MultipartBody.Part>()
                            parts.add(MultipartBody.Part.createFormData("file1" , "file1..jpg", RequestBody.create(MediaType.parse("image/jpeg"), File(reportPhotoUrl!!))))

                            compositeDisposable.add(
                                    userRepository.uploadImages(parts)
                                            .observeOn(scheduler.ui())
                                            .subscribeOn(scheduler.io())
                                            .subscribe({ response ->
                                                fileUuidsReport = response.response?.fileUuids
                                                continueReportTaxiUsage(view)

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
                        else {
                            continueReportTaxiUsage(view)
                        }

                    }
                    .create()
                    .show()
        }
    }

    fun finishTaxiUsage(view: View?) {

        if(selectedDateFinish == null
                || endLocationFinish.value == null
                || descriptionFinish.value == null
                || selectedPurposeTypeFinish == null
                || (if (selectedPurposeTypeFinish?.value == "PERSONAL") false else
                    (payAmountFinish.value == null
                            || finishPhotoUrl == null ))
        ) {

            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.error_password_empty)))
            }
            return
        }

        view?.let {
            TaxiCreateDialog.Builder(it.context)
                    .setTitle(it.context.getString(R.string.finish_taxi_use))
                    .setTextDate(selectedDateTextFinish!!)
                    .setTextFinish(endLocationTextFinish.value?:"")
                    .setTextDescription(descriptionFinish.value!!)
                    .setTextPay(payAmountFinish.value?:"")
                    .setOkButton { dialog ->
                        dialog.dismiss()

                        if(finishPhotoUrl != null) {
                            val parts = ArrayList<MultipartBody.Part>()
                            parts.add(MultipartBody.Part.createFormData("file1" , "file1..jpg", RequestBody.create(MediaType.parse("image/jpeg"), File(finishPhotoUrl!!))))

                            compositeDisposable.add(
                                    userRepository.uploadImages(parts)
                                            .observeOn(scheduler.ui())
                                            .subscribeOn(scheduler.io())
                                            .subscribe({ response ->
                                                fileUuidsFinish = response.response?.fileUuids
                                                continueFinishTaxiUsage(it)

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
                        else {
                            continueFinishTaxiUsage(it)
                        }

                    }
                    .create()
                    .show()
        }
    }

    private fun continueReportTaxiUsage(view: View?) {
        val request = CreateTaxiUsageRequest(
                startLocation =  TaxiLocationModel(startLocationReport.value!!.latitude, startLocationReport.value!!.longitude, startLocationTextReport.value?:""),
                endLocation = TaxiLocationModel(endLocationReport.value!!.latitude, endLocationReport.value!!.longitude, endLocationTextReport.value?:""),
                purposeOfUse =  selectedPurposeTypeReport?.value,
                explanation = descriptionReport.value!!,
                declaredTaxiFare = payAmountReport.value?.toDouble(),
                usageDate = selectedDateReport!!.convertForBackend2(),
                fileUuids = fileUuidsReport
        )

        compositeDisposable.add(
                taxiRepository.createTaxiUsage(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            if(response.error == null) {
                                taxiUsageCreated.value = true
                            }
                            else {
                                navigator?.handleError(Exception(response.error?.message?:view?.context?.getString(R.string.Generic_Error)?:""))
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

    private fun continueFinishTaxiUsage(view: View?) {

        taxiUsage.value?.let { taxiRequest ->
            taxiRequest.endLocation = TaxiLocationModel(endLocationFinish.value!!.latitude, endLocationFinish.value!!.longitude, endLocationTextFinish.value?:"")
            taxiRequest.purposeOfUse = selectedPurposeTypeFinish?.value
            taxiRequest.explanation = descriptionFinish.value!!
            taxiRequest.declaredTaxiFare = payAmountFinish.value?.toDouble()
            taxiRequest.usageDate = selectedDateFinish!!.convertForBackend2()
            taxiRequest.fileUuids = fileUuidsFinish
            taxiUsage.value = taxiRequest
        }

        compositeDisposable.add(
                taxiRepository.updateTaxiUsage(taxiUsage.value!!.id!!, taxiUsage.value!!)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            if(response.error == null) {
                                taxiUsageFinished.value = true
                            }
                            else {
                                navigator?.handleError(Exception(response.error?.message?:view?.context?.getString(R.string.Generic_Error)?:""))
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

    fun getTaxiUsages() {

        compositeDisposable.add(
                taxiRepository.getTaxiUsages()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            taxiUsages.value = response.response
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

    fun getPurposes(langCode: String? = "tr") {
        compositeDisposable.add(
                userRepository.getPurposesOfTaxiUse(langCode)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            purposes.value = response.response
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

    fun startTaxiUsage(view: View?) {

        if(selectedDateStart == null
                || startLocationStart.value == null
                /*|| endLocationStart.value == null*/) {

            view?.let {
                navigator?.handleError(Exception(it.context.getString(R.string.error_password_empty)))
            }
            return
        }

        view?.let {

            FlexigoInfoDialog.Builder(it.context)
                    .setText1(it.context.getString(R.string.start_taxi_usage_dialog_title))
                    .setCancelable(true)
                    .setOkButton(it.context.getString(R.string.Generic_Ok)) { dialog ->
                        dialog.dismiss()

                        val request = CreateTaxiUsageRequest(
                                startLocation =  TaxiLocationModel(startLocationStart.value!!.latitude, startLocationStart.value!!.longitude, startLocationTextStart.value?:""),
                                //endLocation = TaxiLocationModel(endLocationStart.value!!.latitude, endLocationStart.value!!.longitude, endLocationTextStart.value!!),
                                usageDate = selectedDateStart!!.convertForBackend2()
                        )

                        compositeDisposable.add(
                                taxiRepository.createTaxiUsage(request)
                                        .observeOn(scheduler.ui())
                                        .subscribeOn(scheduler.io())
                                        .subscribe({ response ->
                                            taxiUsageStarted.value = true
                                            taxiUsage.value = response.response
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
                    .setCancelButton(it.context.getString(R.string.Generic_Close)) { dialog ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
        }
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

}