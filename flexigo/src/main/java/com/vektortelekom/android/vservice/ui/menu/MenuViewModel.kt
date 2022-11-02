package com.vektortelekom.android.vservice.ui.menu

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.vektor.ktx.data.remote.usermanagement.model.BaseResponse
import com.vektor.ktx.utils.logger.AppLogger
import com.vektor.vshare_api_ktx.model.DocumentRequest
import com.vektor.vshare_api_ktx.model.DocumentResponse
import com.vektor.vshare_api_ktx.model.MultiRatingRequest
import com.vektor.vshare_api_ktx.model.UploadResponse2
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.ChangePasswordRequest
import com.vektortelekom.android.vservice.data.model.CustomerStatusModel
import com.vektortelekom.android.vservice.data.model.InfoUpdateRequest
import com.vektortelekom.android.vservice.data.model.PersonelInfoResponse
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import com.vektortelekom.android.vservice.utils.*
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.joda.time.DateTime
import retrofit2.HttpException
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException
import java.util.ArrayList
import javax.inject.Inject

class MenuViewModel
@Inject
constructor(private val userRepository: UserRepository,
            private val poolCarRepository: PoolCarRepository,
            private val scheduler: SchedulerProvider) : BaseViewModel<MenuNavigator>() {

    var previewPhotoPath: String?= null

    val isAddressNotValid : MutableLiveData<Boolean> = MutableLiveData()

    val personnelDetailsResponse: MutableLiveData<PersonelInfoResponse> = MutableLiveData()

    val profilePhotoUuid: MutableLiveData<String> = MutableLiveData()

    val drivingLicenseNumber: MutableLiveData<String?> = MutableLiveData()
    val drivingLicenceValidityDate: MutableLiveData<String?> = MutableLiveData()
    val drivingLicenceGivenDate: MutableLiveData<String?> = MutableLiveData()

    var mPhotoFileFront = ""
    var mPhotoFileRear = ""
    val uploadDriverLicenseImagesResponse: MutableLiveData<UploadResponse2> = MutableLiveData()

    var isPoolCarActive: Boolean = false

    var isForDrivingLicense: Boolean = false

    var isComingSurvey :  Boolean = false
    var isComingRegistration :  Boolean = false
    var isLocationPermissionSuccess :  Boolean = false

    val addDocumentResponse: MutableLiveData<DocumentResponse> = MutableLiveData()

    val customerStatus: MutableLiveData<CustomerStatusModel> = MutableLiveData()

    val logoutResponse: MutableLiveData<BaseResponse> = MutableLiveData()
    val logutUmngResponse: MutableLiveData<Boolean> = MutableLiveData()

    val latestDrivingLicenceDocument: MutableLiveData<DocumentRequest?> = MutableLiveData()

    var pdfUrl: String? = null

    fun getPersonnelInfo() {
        compositeDisposable.add(
                userRepository.getPersonalDetails()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response.error != null) {
                                navigator?.showLoginActivity()
                            }
                            else {
                                personnelDetailsResponse.value = response
                                name.value = response.response.name.plus("\n").plus(response.response.surname)
                                mail.value = response.response.email
                                phone.value = response.response.phoneNumber
                                company.value = response.response.company?.title
                                address.value = response.response.homeLocation?.address
                            }
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

    val name: MutableLiveData<String> = MutableLiveData()
    val mail: MutableLiveData<String> = MutableLiveData()
    val phone: MutableLiveData<String> = MutableLiveData()
    val company: MutableLiveData<String> = MutableLiveData()
    val address: MutableLiveData<String> = MutableLiveData()
    val editProfileSuccess: MutableLiveData<Boolean> = MutableLiveData()

    fun editProfile(view: View?) {
        if(phone.value.isValidPhoneNumber()) {
            compositeDisposable.add(
                    userRepository.infoUpdate(InfoUpdateRequest(phone.value?:""))
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                if(response.error != null) {
                                    navigator?.handleError(Exception(response.error?.message))
                                }
                                else {
                                    AppDataManager.instance.personnelInfo?.phoneNumber = phone.value?:""
                                    editProfileSuccess.value = true
                                }
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
        else {
            navigator?.handleError(Exception(view?.context?.getString(R.string.phone_number_not_valid)))
        }
    }

    val oldPassword: MutableLiveData<String> = MutableLiveData()
    val newPassword: MutableLiveData<String> = MutableLiveData()
    val newPasswordAgain: MutableLiveData<String> = MutableLiveData()
    val changePasswordSuccess: MutableLiveData<Boolean> = MutableLiveData()

    fun changePassword(view: View?) {

        if(oldPassword.value.isNullOrEmpty() || newPassword.value.isNullOrEmpty() || newPasswordAgain.value.isNullOrEmpty()) {
            navigator?.handleError(Exception(view?.context?.getString(R.string.error_password_empty)))
            return
        }
        if(newPassword.value != newPasswordAgain.value) {
            navigator?.handleError(Exception(view?.context?.getString(R.string.error_old_new_password_not_match)))
            return
        }

        compositeDisposable.add(
                userRepository.changePassword(ChangePasswordRequest(oldPassword.value?:"", newPassword.value?:"", newPasswordAgain.value?:""))
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response.error != null) {
                                navigator?.handleError(Exception(response.error?.message))
                            }
                            else {
                                changePasswordSuccess.value = true
                            }
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

    val homeLocation : MutableLiveData<LatLng> = MutableLiveData()
    val homeLocationSuccess: MutableLiveData<Boolean> = MutableLiveData()

    fun updateAddress(view: View?) {

        homeLocation.value?.let {
            compositeDisposable.add(
                    userRepository.updateUser(it)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                if(response.error != null) {
                                    navigator?.handleError(Exception(response.error?.message))
                                }
                                else {
                                    //isAddressNotValid.value = false
                                    AppDataManager.instance.personnelInfo?.homeLocation = response.response.homeLocation
                                    homeLocationSuccess.value = true
                                }
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

    fun getLatestDrivingLicenceDocument(accountId: String) {

        compositeDisposable.add(
            userRepository.getLatestDrivingLicence(accountId)
                .observeOn(scheduler.ui())
                .subscribeOn(scheduler.io())
                .subscribe({ response ->
                    latestDrivingLicenceDocument.value = response

                }, { ex ->
                    latestDrivingLicenceDocument.value = null
                    println("error: ${ex.localizedMessage}")
                    setIsLoading(false)
                }, {
                    setIsLoading(false)
                }, {
                    setIsLoading(true)
                }
                )
        )
    }

    fun onSoundEffectCheckChanged(checked: Boolean) {
        AppDataManager.instance.isSettingsSoundEffectsEnabled = checked
    }

    fun onNotificationsCheckChanged(checked: Boolean) {
        AppDataManager.instance.isSettingsNotificationsEnabled = checked
    }

    fun onEmailNotificationsCheckChanged(checked: Boolean) {
        AppDataManager.instance.isSettingsEmailNotificationsEnabled = checked
    }

    fun onShowPhoneCheckChanged(checked: Boolean) {
        AppDataManager.instance.isSettingsShowPhoneEnabled = checked
    }

    val privacyPolicy: MutableLiveData<ResponseBody> = MutableLiveData()

    fun getPrivacyPolicy() {

        pdfUrl?.let {
            compositeDisposable.add(
                    userRepository.downloadFileWithDynamicUrlSync(it)
                            .observeOn(scheduler.ui())
                            .subscribeOn(scheduler.io())
                            .subscribe({ response ->
                                privacyPolicy.value = response
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

    fun changeProfilePhoto(imageUrl: String) {

        val parts = ArrayList<MultipartBody.Part>()
        parts.add(MultipartBody.Part.createFormData("file1", "file1" + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), File(imageUrl))))

        compositeDisposable.add(
                userRepository.uploadImages(parts)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            //changeProfilePhotoUuid.value =
                            response.response?.fileUuids?.get(0)?.let { changeProfilePhotoAfterPhotoUpdate(it) }

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

    private fun changeProfilePhotoAfterPhotoUpdate(photoUuid: String) {

        compositeDisposable.add(
                userRepository.changeProfilePhoto(photoUuid)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            if(response.error != null) {
                                navigator?.handleError(Exception(response.error?.message))
                            }
                            else {
                                AppDataManager.instance.personnelInfo?.profileImageUuid = photoUuid
                                profilePhotoUuid.value = photoUuid
                                navigator?.profilePhotoUpdated(photoUuid)
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

    fun sendMultiRating(request: MultiRatingRequest) {
        compositeDisposable.add(
                userRepository.sendMultiRating(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            navigator?.backPressed(null)
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

    fun uploadDriverLicense(view: View?) {

        if (drivingLicenseNumber.value?.trim().isNullOrEmpty()) {
            navigator?.handleError(RuntimeException("Ehliyet No boş geçilemez."))
        } else if (drivingLicenceGivenDate.value.isNullOrEmpty()) {
            navigator?.handleError(RuntimeException("Ehliyet Veriliş Tarihi boş geçilemez."))
        }/* else if(drivingLicenceGivenDate.value.isOlderThanYear(2).not()) {
            navigator?.handleError(RuntimeException("Ehliyet ${2} yıldan eski olmalı."))
        }*/ else if (drivingLicenceValidityDate.value.isNullOrEmpty()) {
            navigator?.handleError(RuntimeException("Ehliyet Geçerlilik Tarihi boş geçilemez."))
        } else if (drivingLicenceValidityDate.value.formatForService2() <= drivingLicenceGivenDate.value.formatForService2()) {
            navigator?.handleError(RuntimeException("Ehliyet geçerlilik tarihi, veriliş tarihinden küçük olamaz."))
        } else if (DateTime.now().formatNow() < drivingLicenceGivenDate.value.formatForService2()) {
            navigator?.handleError(RuntimeException("Ehliyet veriliş tarihi, bugünden büyük olamaz."))
        } else if (mPhotoFileFront.isEmpty() || mPhotoFileRear.isEmpty()) {
            navigator?.handleError(RuntimeException("Fotoğraflar yüklenmedi."))
        } else {
            uploadDriverLicenseImages()
        }

    }

    fun uploadDriverLicenseImages() {

        val images: List<String> = listOf(mPhotoFileFront, mPhotoFileRear)

        val parts = ArrayList<MultipartBody.Part>()
        for (i in images.indices) {
            parts.add(MultipartBody.Part.createFormData("file" + (i + 1), "file" + (i + 1) + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), File(images[i]))))
        }

        val startTime = System.currentTimeMillis()

        compositeDisposable.add(
                poolCarRepository.uploadImages(parts)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            uploadDriverLicenseImagesResponse.value = response

                            if (response.response != null) {

                                AppDataManager.instance.carShareUser?.user?.let { customerStatus ->
                                    val request = DocumentRequest()
                                    val uploadImagesUuids = response.response!!.fileUuids
                                    for (i in 0..1) {
                                        if (i == 0) {
                                            request.photoUuid1 = uploadImagesUuids?.get(i)
                                        } else {
                                            request.photoUuid2 = uploadImagesUuids?.get(i)
                                        }
                                    }
                                    request.documentType = AppConstants.DocumentType.DRIVING_LICENSE
                                    request.referenceId = customerStatus.accountId.toLong()
                                    request.identityNumber = customerStatus.tckn
                                    request.issuedBy = "TR"
                                    //
                                    request.documentNumber = drivingLicenseNumber.value
                                    request.validUntil = drivingLicenceValidityDate.value.formatForService2()
                                    request.issueDate = drivingLicenceGivenDate.value.formatForService2()
                                    //
                                    addDocument(request)
                                }

                            } else if (response.error != null) {
                                //showToast(getString(R.string.Generic_err))
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

    fun addDocument(request: DocumentRequest) {
        compositeDisposable.add(
                userRepository.addDocument(request)
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            addDocumentResponse.value = response
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

    fun getCustomerStatus() {

        compositeDisposable.add(
                poolCarRepository.getCustomerStatus()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
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

    fun logout() {

        compositeDisposable.add(
                userRepository.logout()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->

                            if(response.error == null) {
                                logoutResponse.value = response


                            }
                            else {
                                navigator?.handleError(Exception(response.error?.message?:""))
                            }

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

    fun logoutUmng() {

        compositeDisposable.add(
                userRepository.logoutUmng()
                        .observeOn(scheduler.ui())
                        .subscribeOn(scheduler.io())
                        .subscribe({ response ->
                            logutUmngResponse.value = true
                        }, { ex ->

                        }, {
                        }, {
                        }
                        )
        )
    }

}