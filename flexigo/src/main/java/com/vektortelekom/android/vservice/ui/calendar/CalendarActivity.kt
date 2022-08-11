package com.vektortelekom.android.vservice.ui.calendar

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.CalendarActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.calendar.dialog.CalendarAccountsDialog
import com.vektortelekom.android.vservice.ui.calendar.fragment.CalendarMainFragment
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import javax.inject.Inject

class CalendarActivity : BaseActivity<CalendarViewModel>(), CalendarNavigator {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CalendarViewModel

    private lateinit var binding: CalendarActivityBinding

    // Google
    private lateinit var googleClient: GoogleSignInClient

    // Outlook
    val scopes = arrayOf("Calendars.Read")
    var mMultipleAccountApp: IMultipleAccountPublicClientApplication? = null
    var mFirstAccount: IAccount? = null

    private lateinit var handler: Handler

    var calendarAccountsDialog: CalendarAccountsDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<CalendarActivityBinding>(this, R.layout.calendar_activity).apply {
            lifecycleOwner = this@CalendarActivity
            viewModel = this@CalendarActivity.viewModel
        }
        viewModel.navigator = this

        showCalendarsMainFragment(null)

        handler = Handler()

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder().requestScopes(Scope("https://www.googleapis.com/auth/calendar"))
                .requestServerAuthCode(BuildConfig.GOOGLE_SERVICE_ACCOUNT, true)
                .requestEmail()
                .build()
        googleClient = GoogleSignIn.getClient(this, gso)


    }

    private fun showCalendarsMainFragment(view: View?) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, CalendarMainFragment.newInstance(), CalendarMainFragment.TAG)
                .commit()
    }

    override fun showCalendarAccountsFragment(view: View?) {
        calendarAccountsDialog = CalendarAccountsDialog(this, this, object: CalendarAccountsDialog.CalendarAccountsListener {
            override fun googleSignIn() {
                val intent = googleClient.signInIntent
                startActivityForResult(intent, 112)
            }

            override fun googleSignOut() {
                googleClient.signOut().addOnSuccessListener {
                    calendarAccountsDialog?.setGoogleSignInView(false)
                    AppDataManager.instance.googleCalendarAccessToken = null
                    AppDataManager.instance.googleCalendarEmail = null

                    calendarAccountsDialog?.dismiss()
                    calendarAccountsDialog = null
                    //viewModel.navigator?.backPressed(null)

                    viewModel.googleCalendarEmail.value = null
                    viewModel.googleCalendarAccessToken.value = null

                }
                        .addOnFailureListener {
                            Toast.makeText(this@CalendarActivity, "Sign out failure", Toast.LENGTH_SHORT).show()
                        }
            }

            override fun outlookSignIn() {

                val configFile = R.raw.msal_config

                PublicClientApplication.createMultipleAccountPublicClientApplication(this@CalendarActivity,
                        configFile,
                        object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                            override fun onCreated(application: IMultipleAccountPublicClientApplication?) {


                                mMultipleAccountApp = application

                                mMultipleAccountApp?.acquireToken(this@CalendarActivity, scopes, getAuthInteractiveCallback())

                            }

                            override fun onError(exception: MsalException?) {
                                //Log Exception Here
                            }
                        })
            }

            override fun outlookSignOut() {
                AppDataManager.instance.outlookCalendarEmail?.let {

                    val configFile = R.raw.msal_config

                    PublicClientApplication.createMultipleAccountPublicClientApplication(this@CalendarActivity,
                            configFile,
                            object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                                override fun onCreated(application: IMultipleAccountPublicClientApplication?) {

                                    Thread {

                                        val account: IAccount? = application?.getAccount(it)

                                        account?.let { outlookAccount ->

                                            application.removeAccount(outlookAccount)

                                            runOnUiThread {
                                                calendarAccountsDialog?.setOutlookSignInView(false)
                                                AppDataManager.instance.outlookCalendarEmail = null
                                                AppDataManager.instance.outlookCalendarAccessToken = null

                                                calendarAccountsDialog?.dismiss()
                                                calendarAccountsDialog = null
                                                //viewModel.navigator?.backPressed(null)

                                                viewModel.outlookCalendarEmail.value = null
                                                viewModel.outlookCalendarAccessToken.value = null
                                            }

                                        }


                                    }.start()

                                }

                                override fun onError(exception: MsalException?) {
                                    //Log Exception Here
                                }
                            })

                }
            }

        })

        calendarAccountsDialog?.show()
        /*supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, CalendarAccountsFragment.newInstance(), CalendarAccountsFragment.TAG)
                .addToBackStack(null)
                .commit()*/
    }

    override fun showMenuActivity(view: View?) {
        startActivity(Intent(this, MenuActivity::class.java))
    }


    override fun getViewModel(): CalendarViewModel {
        viewModel = ViewModelProvider(this, factory)[CalendarViewModel::class.java]
        return viewModel
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    data class CalendarItem(
            val startMinutes: Int,
            val endMinutes: Int,
            val title: String,
            val startTime: Long,
            val endTime: Long,
            val location: String?
    )


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 112) {

            val signinResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (signinResult?.isSuccess == true) {
                signinResult.signInAccount?.let {

                    it.serverAuthCode?.let { authCode ->
                        viewModel.sendGoogleAuthCode(authCode)
                    }

                    Thread {

                        val scope = "oauth2:" + "https://www.googleapis.com/auth/calendar"
                        val account = Account(it.email, "com.google")
                        val token: String = GoogleAuthUtil.getToken(this, account, scope)

                        AppDataManager.instance.googleCalendarEmail = it.email
                        AppDataManager.instance.googleCalendarAccessToken = token

                        runOnUiThread {

                            calendarAccountsDialog?.dismiss()
                            calendarAccountsDialog = null
                            //viewModel.navigator?.backPressed(null)

                            viewModel.googleCalendarEmail.value = it.email
                            viewModel.googleCalendarAccessToken.value = token
                        }


                        /*handler.post {
                            setGoogleSignInView(true)
                        }*/

                        //getEventList(token)


                    }.start()


                }
            }

        }
    }

    private fun getAuthInteractiveCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                /* Successfully got a token, use it to call a protected resource */
                val accessToken: String = authenticationResult.accessToken
                // Record account used to acquire token
                mFirstAccount = authenticationResult.account

                AppDataManager.instance.outlookCalendarEmail = mFirstAccount?.id
                AppDataManager.instance.outlookCalendarAccessToken = accessToken

                calendarAccountsDialog?.dismiss()
                calendarAccountsDialog = null
                //viewModel.navigator?.backPressed(null)

                viewModel.outlookCalendarEmail.value = mFirstAccount?.id
                viewModel.outlookCalendarAccessToken.value = accessToken
            }

            override fun onError(exception: MsalException?) {
                if (exception is MsalClientException) {
                    //And exception from the client (MSAL)
                } else if (exception is MsalServiceException) {
                    //An exception from the server
                }
            }

            override fun onCancel() {
                /* User canceled the authentication */
            }
        }
    }


}