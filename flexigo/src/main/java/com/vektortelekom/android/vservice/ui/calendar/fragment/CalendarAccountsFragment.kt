package com.vektortelekom.android.vservice.ui.calendar.fragment

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.Scope
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.CalendarAccountsFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.calendar.CalendarViewModel
import javax.inject.Inject

class CalendarAccountsFragment: BaseFragment<CalendarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CalendarViewModel

    lateinit var binding: CalendarAccountsFragmentBinding

    // Google
    private lateinit var googleClient: GoogleSignInClient

    // Outlook
    val scopes = arrayOf("Calendars.Read")
    var mMultipleAccountApp: IMultipleAccountPublicClientApplication? = null
    var mFirstAccount: IAccount? = null

    private lateinit var handler: Handler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CalendarAccountsFragmentBinding>(inflater, R.layout.calendar_accounts_fragment, container, false).apply {
            lifecycleOwner = this@CalendarAccountsFragment
            viewModel = this@CalendarAccountsFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder().requestScopes(Scope("https://www.googleapis.com/auth/calendar"))
                .requestServerAuthCode(BuildConfig.GOOGLE_SERVICE_ACCOUNT, true)
                .requestEmail()
                .build()
        googleClient = GoogleSignIn.getClient(requireActivity(), gso)

        setGoogleSignInView(AppDataManager.instance.googleCalendarAccessToken.isNullOrEmpty().not())
        setOutlookSignInView(AppDataManager.instance.outlookCalendarAccessToken.isNullOrEmpty().not())

    }

    private fun setGoogleSignInView(isSignIn: Boolean) {
        if (isSignIn) {
            binding.buttonGoogleSignIn.visibility = View.GONE
            binding.buttonGoogleSignOut.visibility = View.VISIBLE

            binding.buttonGoogleSignOut.setOnClickListener {
                googleClient.signOut().addOnSuccessListener {
                    setGoogleSignInView(false)
                    AppDataManager.instance.googleCalendarAccessToken = null
                    AppDataManager.instance.googleCalendarEmail = null

                    viewModel.navigator?.backPressed(null)

                    viewModel.googleCalendarEmail.value = null
                    viewModel.googleCalendarAccessToken.value = null

                }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Sign out failure", Toast.LENGTH_SHORT).show()
                        }

            }
        } else {
            binding.buttonGoogleSignOut.visibility = View.GONE
            binding.buttonGoogleSignIn.visibility = View.VISIBLE

            binding.buttonGoogleSignIn.setSize(SignInButton.SIZE_STANDARD)

            binding.buttonGoogleSignIn.setOnClickListener {
                val intent = googleClient.signInIntent
                startActivityForResult(intent, 112)
            }
        }
    }

    private fun setOutlookSignInView(isSignIn: Boolean) {
        if (isSignIn) {
            binding.buttonOutlookSignIn.visibility = View.GONE
            binding.buttonOutlookSignOut.visibility = View.VISIBLE

            binding.buttonOutlookSignOut.setOnClickListener {

                AppDataManager.instance.outlookCalendarEmail?.let {

                    val configFile = R.raw.msal_config

                    PublicClientApplication.createMultipleAccountPublicClientApplication(requireContext(),
                            configFile,
                            object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                                override fun onCreated(application: IMultipleAccountPublicClientApplication?) {

                                    Thread {

                                        val account: IAccount? = application?.getAccount(it)

                                        account?.let { outlookAccount ->

                                            application.removeAccount(outlookAccount)

                                            activity?.runOnUiThread {
                                                setOutlookSignInView(false)
                                                AppDataManager.instance.outlookCalendarEmail = null
                                                AppDataManager.instance.outlookCalendarAccessToken = null

                                                viewModel.navigator?.backPressed(null)

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
        } else {
            binding.buttonOutlookSignOut.visibility = View.GONE
            binding.buttonOutlookSignIn.visibility = View.VISIBLE

            val configFile = R.raw.msal_config

            binding.buttonOutlookSignIn.setOnClickListener {
                PublicClientApplication.createMultipleAccountPublicClientApplication(requireContext(),
                        configFile,
                        object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                            override fun onCreated(application: IMultipleAccountPublicClientApplication?) {


                                mMultipleAccountApp = application

                                mMultipleAccountApp?.acquireToken(requireActivity(), scopes, getAuthInteractiveCallback())
                            }

                            override fun onError(exception: MsalException?) {
                                //Log Exception Here
                            }
                        })
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

                viewModel.navigator?.backPressed(null)

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
                        val token: String = GoogleAuthUtil.getToken(requireContext(), account, scope)

                        AppDataManager.instance.googleCalendarEmail = it.email
                        AppDataManager.instance.googleCalendarAccessToken = token

                        activity?.runOnUiThread {

                            viewModel.navigator?.backPressed(null)

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


    override fun getViewModel(): CalendarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CalendarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CalendarAccountsFragment"

        fun newInstance() = CalendarAccountsFragment()

    }

}