package com.vektortelekom.android.vservice.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.LoginActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.login.fragment.ForgotPasswordFragment
import com.vektortelekom.android.vservice.ui.login.fragment.LoginFragment
import com.vektortelekom.android.vservice.ui.login.fragment.LoginFragmentFactory
import com.vektortelekom.android.vservice.ui.survey.SurveyActivity
import com.vektortelekom.android.vservice.utils.AnalyticsManager
import javax.inject.Inject

class LoginActivity : BaseActivity<LoginViewModel>(), LoginNavigator  {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: LoginViewModel

    private lateinit var binding: LoginActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = LoginFragmentFactory()
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<LoginActivityBinding>(this, R.layout.login_activity).apply {
            lifecycleOwner = this@LoginActivity
        }
        viewModel.navigator = this
        stateManager.logout()

        viewModel.langCode = resources.configuration.locale.language

        createObservers()

        showLoginFragment(null)
    }

    private fun createObservers() {

        viewModel.loginResponse.observe(this) { response ->
            stateManager.vektorToken = response.sessionId
            AnalyticsManager.build(this).setUserId(response.personnel.id.toString())
            AppDataManager.instance.personnelInfo = response.personnel
            AppDataManager.instance.rememberMe = viewModel.isRememberMe.value

            if (viewModel.isRememberMe.value == true) {
                AppDataManager.instance.userName = viewModel.loginEmail.value
                AppDataManager.instance.password = viewModel.loginPassword.value
            } else {
                AppDataManager.instance.userName = null
                AppDataManager.instance.password = null
            }
            response.surveyQuestionId?.let {
                val intent = Intent(this, SurveyActivity::class.java)
                intent.putExtra("surveyQuestionId", it)
                intent.putExtra("isCommuteOptionsEnabled", viewModel.isCommuteOptionsEnabled)
                startActivity(intent)
            } ?: run {
                showHomeActivity()
            }

            finish()
        }

    }

    override fun showLoginFragment(view: View?) {

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, LoginFragment.newInstance(), LoginFragment.TAG)
                .commit()

    }

    override fun showForgotPasswordFragment(view: View?) {

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, ForgotPasswordFragment.newInstance(), ForgotPasswordFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun tryLoginWithOtherServer(username: String, password: String, isFirstTry: Boolean) {
        if (stateManager.baseURL == BuildConfig.BASE_URL) {
            stateManager.baseURL = BuildConfig.BASE_URL_US
        }
        else {
            stateManager.baseURL = BuildConfig.BASE_URL
        }
        viewModel.loginEmail.value = username
        viewModel.loginPassword.value = password
        viewModel.login(null, false)
    }

    override fun getViewModel(): LoginViewModel {
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
        return viewModel
    }
}
