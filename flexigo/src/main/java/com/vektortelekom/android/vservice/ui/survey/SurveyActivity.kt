package com.vektortelekom.android.vservice.ui.survey

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.SurveyAnswerRequest
import com.vektortelekom.android.vservice.databinding.SurveyActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import com.vektortelekom.android.vservice.ui.survey.fragment.SurveyFragment
import com.vektortelekom.android.vservice.ui.survey.fragment.VanPoolLocationPermissionFragment
import javax.inject.Inject

class SurveyActivity : BaseActivity<SurveyViewModel>(), SurveyNavigator, PermissionsUtils.LocationStateListener {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: SurveyViewModel

    private lateinit var binding: SurveyActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<SurveyActivityBinding>(this, R.layout.survey_activity).apply {
            lifecycleOwner = this@SurveyActivity
        }

        viewModel.navigator = this
        viewModel.questionId.value = intent.getIntExtra("surveyQuestionId",0)

        viewModel.isSurveyFirstScreen = true
        viewModel.isContinueButtonEnabled.value = true
        showSurveyFragment()
        //viewModel.getSurveyQuestion(70645001)

        viewModel.surveyQuestion.observe(this) {
            showSurveyFragment()
        }
        viewModel.isContinueButtonEnabled.observe(this) {
            if (it != null) {
                binding.buttonContinue.isEnabled = it
            }
        }

        binding.buttonContinue.setOnClickListener {
            if (viewModel.questionId.value != null && !viewModel.isSurveyFirstScreen) {
                val questionId = viewModel.questionId.value
                val answerIds = viewModel.selectedAnswers.value
                val secondaryAnswersIds = viewModel.secondaryAnswers.value

                if (answerIds != null) {
                    if (answerIds.isNotEmpty()) {
                        viewModel.uploadSurveyAnswers(SurveyAnswerRequest(questionId = questionId!!, answerIds = answerIds, secondaryModeAnswerIds = secondaryAnswersIds!!))
                    }
                }
            } else if (viewModel.questionId.value != null){
                viewModel.getSurveyQuestion(viewModel.questionId.value!!)
            }
            else{
                if (checkAndRequestLocationPermission(this)) {
                    onLocationPermissionOk()
                }
            }
        }

    }

    override fun showSurveyFragment() {

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, SurveyFragment.newInstance(), SurveyFragment.TAG)
                .commit()

    }
    override fun showVanPoolLocationPermissionFragment() {

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, VanPoolLocationPermissionFragment.newInstance(), VanPoolLocationPermissionFragment.TAG)
                .commit()

    }

    override fun showMenuAddressesFragment() {

        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("is_coming_survey", true)
        intent.putExtra("is_location_permission_success", viewModel.isLocationPermissionSuccess)
        startActivity(intent)

        finish()
    }

    override fun getViewModel(): SurveyViewModel {
        viewModel = ViewModelProvider(this, factory).get(SurveyViewModel::class.java)
        return viewModel
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun reloadFragment() {
        val fragment = supportFragmentManager.findFragmentByTag(SurveyFragment.TAG)

        fragment?.let {
            supportFragmentManager
                .beginTransaction()
//                .detach(it)
                .attach(it)
                .commit()
        }

    }

    override fun onLocationPermissionOk() {
        viewModel.isLocationPermissionSuccess = true
        viewModel.navigator?.showMenuAddressesFragment()
    }

    override fun onLocationPermissionFailed() {
        viewModel.isLocationPermissionSuccess = false
        viewModel.navigator?.showMenuAddressesFragment()
    }

}