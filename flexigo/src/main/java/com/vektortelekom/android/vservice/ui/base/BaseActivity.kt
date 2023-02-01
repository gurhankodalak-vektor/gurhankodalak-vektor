package com.vektortelekom.android.vservice.ui.base

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.vektor.ktx.data.local.StateManager
import com.vektor.ktx.data.remote.model.BaseErrorModel
import com.vektor.ktx.ui.dialog.AppProgressBar
import com.vektor.ktx.ui.dialog.WaitingDialog
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.ktx.utils.RootUtil
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.BuildConfig

import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.CheckDomainRequest
import com.vektortelekom.android.vservice.data.model.NotificationModel
import com.vektortelekom.android.vservice.data.model.RegisterVerifyCompanyCodeRequest
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.home.HomeActivity
import com.vektortelekom.android.vservice.ui.login.LoginActivity
import com.vektortelekom.android.vservice.ui.registration.RegistrationActivity
import com.vektortelekom.android.vservice.ui.splash.SplashActivity
import com.vektortelekom.android.vservice.utils.AppConstants
import com.vektortelekom.android.vservice.utils.fcm.AppFirebaseMessagingService
import dagger.android.support.DaggerAppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.regex.Pattern
import javax.inject.Inject

abstract class BaseActivity<T : BaseViewModel<*>> : DaggerAppCompatActivity(), BaseNavigator {

    private var viewModel: T? = null

    @Inject
    lateinit var stateManager: StateManager

    abstract fun getViewModel(): T

    private val pd: AppProgressBar = AppProgressBar()

    private var PERMISSION_REQUEST_CODE = -1
    val PERMISSION_CAMERA = 72
    private val PERMISSION_LOCATION = 73

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.viewModel = if (viewModel == null) getViewModel() else viewModel

        this.viewModel?.isLoading?.observe(this) { value ->
            if (value)
                showPd()
            else
                dismissPd()
        }

        this.viewModel?.sessionExpireError?.observe(this) {
            stateManager.logout()
            AppDataManager.instance.logout()
            showLoginActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    fun checkDeviceRootedState(function: () -> Unit) {
        if (this is SplashActivity || this is HomeActivity) {
            if (RootUtil.isDeviceRooted()) {
                doActionWhenDeviceRooted()
                return
            }
        }

        function.invoke()
    }

    override fun moveNext(view: View?) {
    }

    override fun movePrevious(view: View?) {
    }

    fun showPd(message: String) {
        try {
            if (!isFinishing && !isDestroyed) {
                WaitingDialog.show(this)
//                if (pd.dialog == null || !pd.dialog!!.isShowing)
//                    pd.show(this, message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showPd() {
        try {
            if (!isFinishing && !isDestroyed) {
                WaitingDialog.show(this)
//                if (pd.dialog == null || !pd.dialog!!.isShowing)
//                    pd.show(this, resources.getString(R.string.Loading))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showPd(cancelable: Boolean) {
        try {
            if (!isFinishing && !isDestroyed) {
                WaitingDialog.show(this, cancelable)
//                if (pd.dialog == null || !pd.dialog!!.isShowing)
//                    pd.show(this, resources.getString(R.string.Loading), cancelable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissPd() {
        try {
            if (!isFinishing && !isDestroyed)
                WaitingDialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun removeFragment(tag: String) {
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            fragmentManager
                    .beginTransaction()
                    .disallowAddToBackStack()
                    .remove(fragment)
                    .commitNow()
        }
        //.setCustomAnimations(R.anim.slide_left, R.anim.slide_right)
    }

    fun addFragment(@IdRes containerViewId: Int, @NonNull fragment: Fragment, @Nullable tag: String) {
        supportFragmentManager
                .beginTransaction()
                .disallowAddToBackStack()
                .add(containerViewId, fragment, tag)
                .commit()
        //.setCustomAnimations(R.anim.slide_left, R.anim.slide_right)
    }

    fun replaceFragment(@IdRes containerViewId: Int, @NonNull fragment: Fragment, @Nullable tag: String) {
        supportFragmentManager
                .beginTransaction()
                .disallowAddToBackStack()
                .replace(containerViewId, fragment, tag)
                .commitAllowingStateLoss()
        //.setCustomAnimations(R.anim.slide_left, R.anim.slide_right)
    }

    override fun onFragmentDetached(tag: String) {
        removeFragment(tag)
    }

    override fun showLoginActivity() {
        finishAffinity()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun showRegisterActivity() {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    override fun showHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    override fun tryLoginWithOtherServer(username: String, password: String, isFirstTry: Boolean) {
        //override this when you need it
    }

    override fun tryCheckDomainWithOtherServer(checkDomainRequest: CheckDomainRequest, langCode: String) {
        //override this when you need it
    }

    override fun tryCompanyCodeWithOtherServer(
        registerVerifyCompanyCodeRequest: RegisterVerifyCompanyCodeRequest,
        langCode: String
    ) {
        //override this when you need it
    }

    private fun getMessageFromError(error: Throwable): String {
        var message: String? = null

        when (error) {
            is HttpException -> {
                if (error.response()?.code() == 403) {
                    stateManager.logout()
                    AppDataManager.instance.logout()
                    showLoginActivity()
                    finishAffinity()
                    AppLogger.w("Error: ${error.localizedMessage}")
                } else {
                    var baseErrorModel: BaseErrorModel? = null
                    try {
                        val responseBody = error.response()!!.errorBody()
                        val gson = Gson()
                        baseErrorModel = gson.fromJson(responseBody!!.string(), BaseErrorModel::class.java)
                    } catch (t: Throwable) {
                        AppLogger.e(t, "API Response Parse Error")
                    }
                    message = if (baseErrorModel?.error?.message != null) {
                        baseErrorModel.error?.message
                    } else {
                        resources.getString(R.string.Generic_Error)
                    }
                    AppLogger.w("Error: $message")
                }
            }
            is SocketTimeoutException -> {
                AppLogger.w("Error: ${error.localizedMessage}")
                message = resources.getString(R.string.Generic_Network_Error)
            }
            is IOException -> {
                AppLogger.w("Error: ${error.localizedMessage}")
                message = resources.getString(R.string.Generic_Network_Error)
            }
            else -> {
                AppLogger.w("Error: ${error.localizedMessage}")
                message = error.localizedMessage
            }
        }

        if (message.isNullOrBlank())
            message = resources.getString(R.string.Generic_Error)

        return message
    }

    private fun showErrorMessage(message: String) {
        val dialog = AppDialog.Builder(this)
                .setIconVisibility(true)
                .setTitle(getString(R.string.Generic_Err))
                .setSubtitle(message)
                .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                    dialog.dismiss()
                }
                .create()

        dialog.show()
    }

    override fun handleError(error: Throwable) {
        showErrorMessage(getMessageFromError(error))
    }

    fun setupBottomNavigationBar(navigation: BottomNavigationView) {

    }

    private fun showShouldLoginDialog() {
//        AppDialog.Builder(this)
//                .setIconVisibility(false)
//                .setTitle(getString(R.string.Home_sign_in))
//                .setSubtitle(getString(R.string.BaseActivity_need_to_sign_in))
//                .setOkButton(getString(R.string.Home_sign_in)) { d ->
//                    d.dismiss()
//                    val intent = Intent(this, LoginActivity::class.java)
//                    startActivity(intent)
//                }
//                .setCancelButton(getString(R.string.Generic_close)) { d ->
//                    d.dismiss()
//                }
//                .create().show()
    }

    private fun doActionWhenDeviceRooted() {
        val dialog = AlertDialog.Builder(this)
        dialog.setCancelable(false)
        dialog.setTitle(resources.getString(R.string.rooted_device_title))
        dialog.setMessage(resources.getString(R.string.rooted_device_message))
        dialog.setPositiveButton(resources.getString(R.string.rooted_device_button)) { d, _ ->
            d.dismiss()
            finishAffinity()
        }
        dialog.show()
    }

    fun navigateToMap(userLocationLatitude: Double, userLocationLongitude: Double, targetLocationLatitude: Double, targetLocationLongitude: Double) {
        val baseUri = "geo:%s,%s?q=%s,%s"
//        if (isWalkingMode)
//            baseUri = "geo:%s,%s?q=%s,%s&mode=w"
        val uri = String.format(baseUri, userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            //AppLogger.e("NavigationAppNotFound", e.printStackTrace())
            try {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e1: Exception) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(java.lang.String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)))
                    startActivity(browserIntent)
                } catch (e2: java.lang.Exception) {
                    AppLogger.e(e, "NavigationAppNotFound")
                    Toast.makeText(this, R.string.Maps_No_Exist, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
        val matcher = emailPattern.matcher(email)
        return matcher.find()
    }

    /*fun makeCall(activity: Activity, phoneNumber: String) {
        val i = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CALL_PHONE), 0)
                return
            }
        }
        startActivity(i)
    }*/

    fun showErrorDoorDialog(errorMessage: String) {
        AppDialog.Builder(this)
                .setCloseButtonVisibility(false)
                .setIconVisibility(true)
                .setIcon(R.drawable.ic_warning)
                .setTitle(getString(R.string.Generic_Err))
                .setSubtitle(errorMessage)
                .setOkButton(getString(R.string.call_call_center)) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(AppConstants.System.CALL_CENTER_NUMBER)))
                    startActivity(intent)
                }.create().show()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(event: AppFirebaseMessagingService.NotificationEvent) {
        try {

            if (this is SplashActivity) {
                return
            }

            if(event.data.containsKey("extra")) {

                val model = Gson().fromJson<NotificationModel>(event.data["extra"], object: TypeToken<NotificationModel>() {}.type)

                /*when(model.subCategory) {
                    "ANNOUNCE" -> {



                    }
                    "TICKET_OTHER" -> {

                    }
                }*/
                FlexigoInfoDialog.Builder(this)
                        .setTitle(model.title)
                        .setText1(model.message)
                        .setCancelable(true)
                        .setOkButton(getString(R.string.Generic_Ok)) {
                            it.dismiss()
                        }
                        .create()
                        .show()

                if(this is HomeActivity) {
                    (this as HomeActivity).addNotification(model)
                }
            }

            /*val showPopup = event.data["showPopup"]?.toBoolean()
            if (showPopup != null && showPopup) {
                val title = event.data["title"].toString()
                val message = event.data["message"].toString()

                AppDialog.Builder(this)
                        .setCloseButtonVisibility(false)
                        .setIconVisibility(false)
                        .setTitle(title)
                        .setSubtitle(message)
                        .setOkButton(getString(R.string.Generic_Ok)) { d ->
                            d.dismiss()
                            //Uygulamayı yeniden başlat
                        }.create().show()
            }*/
//            else if (event.notificationMessage != null) {
//                val message = event.notificationMessage.toString()
//                handleNotification(message)
//            }

            if (event.data.containsKey("userUpdated")) {
                updateCustomerStatus()
            }

            EventBus.getDefault().removeStickyEvent(event)

        } catch (e: Exception) {
            Timber.e(e, "onEvent failed")
        }

    }

    open fun updateCustomerStatus() {

    }

    fun checkAndRequestCameraPermission(listener: PermissionsUtils.CameraStateListener): Boolean {
        PERMISSION_REQUEST_CODE = PERMISSION_CAMERA
        if (PermissionsUtils.isCameraPermissionOk(this)) {
            return true
        } else {
            if (PermissionsUtils.isCameraPermissionDenied(this)) {
                AppDialog.Builder(this)
                        .setIconVisibility(true)
                        .setTitle(R.string.permission_camera_title)
                        .setSubtitle(R.string.permission_camera_subtitle)
                        .setOkButton(R.string.settings) { d ->
                            d.dismiss()
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri: Uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            } catch (t: Throwable) {
                                AppLogger.e("Application permission navigation failed.", t)
                            }
                        }
                        .create().show()
            } else {
                PermissionsUtils.checkCameraPermission(this, listener)
            }
            return false
        }
    }

    fun checkAndRequestLocationPermission(listener: PermissionsUtils.LocationStateListener): Boolean {
        PERMISSION_REQUEST_CODE = PERMISSION_LOCATION
        if (PermissionsUtils.isLocationPermissionOk(this)) {
            return true
        } else {
            if (PermissionsUtils.isLocationPermissionDenied(this)) {
                AppDialog.Builder(this)
                        .setIconVisibility(true)
                        .setTitle(R.string.permission_location_title)
                        .setSubtitle(R.string.permission_location_subtitle)
                        .setOkButton(R.string.settings) { d ->
                            d.dismiss()
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri: Uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            } catch (t: Throwable) {
                                AppLogger.e("Application permission navigation failed.", t)
                            }
                        }
                        .create().show()
            } else {
                PermissionsUtils.checkLocationPermission(this, listener)
            }
            return false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)

        when(PERMISSION_REQUEST_CODE) {
            PERMISSION_CAMERA -> {
                for(fragment in supportFragmentManager.fragments) {
                    if(fragment is PermissionsUtils.CameraStateListener) {
                        if(granted) {
                            fragment.onCameraPermissionOk()
                        }
                        else {
                            fragment.onCameraPermissionFailed()
                        }
                    }
                }
            }
            PERMISSION_LOCATION -> {
                for(fragment in supportFragmentManager.fragments) {
                    if(fragment is PermissionsUtils.LocationStateListener) {
                        if(granted) {
                            fragment.onLocationPermissionOk()
                        }
                        else {
                            fragment.onLocationPermissionFailed()
                        }
                    }
                }
            }
        }
    }

}