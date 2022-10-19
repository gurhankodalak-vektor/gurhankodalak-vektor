package com.vektortelekom.android.vservice.ui.splash

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.survey.SurveyActivity
import com.vektortelekom.android.vservice.utils.AnalyticsManager
import com.vektortelekom.android.vservice.utils.AppConstants
import javax.inject.Inject

class SplashActivity: BaseActivity<SplashViewModel>(), SplashNavigator {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        viewModel.navigator = this

        viewModel.checkVersionResponse.observe(this) { response ->
            try {
                if (response?.response?.forceVersion != null) {
                    val packageInfo = packageManager.getPackageInfo(packageName, 0)
                    val version: Long = Integer.parseInt(response.response?.forceVersion!!).toLong()

                    @Suppress("DEPRECATION")
                    val currentVersion: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        packageInfo.versionCode.toLong()
                    }
                    if (version > currentVersion) {
                        AppDialog.Builder(this)
                            .setIconVisibility(true)
                            .setTitle(getString(R.string.splash_version_dialog_title))
                            .setSubtitle(getString(R.string.splash_version_dialog_subtitle))
                            .setOkButton(getString(R.string.generic_upgrade)) { dialog ->
                                openApp(
                                    this@SplashActivity,
                                    packageInfo.applicationInfo.packageName
                                )
                                dialog.dismiss()
                                finish()
                            }
                            .create()
                            .show()
                    } else {
                        if (stateManager.isLoggedIn) {  //version is okay. check is logged in
                            viewModel.getPersonnelInfo()
                            viewModel.getMobileParameters()
                        } else {
                            showLoginActivity()
                        }
                    }
                } else {
                    AppDialog.Builder(this)
                        .setIconVisibility(true)
                        .setTitle(getString(R.string.generic_error_title))
                        .setSubtitle(getString(R.string.splash_version_check_failed))
                        .setOkButton(getString(R.string.generic_retry)) { dialog ->
                            dialog.dismiss()
                            checkVersion()
                        }
                        .create()
                        .show()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                handleError(e)
            }
        }

        viewModel.personnelDetailsResponse.observe(this) {
            AppDataManager.instance.personnelInfo = it.response
            AnalyticsManager.build(this).setUserId(it.response.id.toString())
            getDeviceToken()
        }

        checkDeviceRootedState {
            viewModel.checkVersion(AppConstants.System.APP_NAME, "ANDROID")
        }

    }

    private fun openApp(context: Context, packageName: String): Boolean {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            return true
        } catch (ex: ActivityNotFoundException) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                return true
            } catch (t: Throwable) {
                AppLogger.e(t, "openApp failed")
            }
        }
        return false
    }

    private fun getDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            continueToHome(instanceIdResult.token)
        }.addOnFailureListener {
            continueToHome("")
        }
    }

    private fun continueToHome(firebaseToken: String) {
        viewModel.updateFirebaseToken(firebaseToken)

        if (viewModel.personnelDetailsResponse.value?.response?.destination?.id == 0L){
            showRegisterActivity()
        } else{
            viewModel.personnelDetailsResponse.value?.response?.surveyQuestionId?.let {
                val intent = Intent(this, SurveyActivity::class.java)
                intent.putExtra("surveyQuestionId", it)
                startActivity(intent)
            } ?: run {
                showHomeActivity()
            }
        }


        finish()
    }

    private fun checkVersion() {
        viewModel.checkVersion(AppConstants.System.APP_NAME, "ANDROID")
    }

    override fun getViewModel(): SplashViewModel {
        viewModel = ViewModelProvider(this, factory).get(SplashViewModel::class.java)
        return viewModel
    }
}
