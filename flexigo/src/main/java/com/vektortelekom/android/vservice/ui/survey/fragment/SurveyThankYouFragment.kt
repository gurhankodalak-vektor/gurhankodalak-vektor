package com.vektortelekom.android.vservice.ui.survey.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.SurveyThankYouFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.survey.SurveyViewModel
import javax.inject.Inject


class SurveyThankYouFragment: BaseFragment<SurveyViewModel>(){

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: SurveyViewModel
    private lateinit var buttonContinue: AppCompatButton

    lateinit var binding: SurveyThankYouFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<SurveyThankYouFragmentBinding>(inflater, R.layout.survey_thank_you_fragment, container, false).apply {
            lifecycleOwner = this@SurveyThankYouFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonContinue = requireActivity().findViewById<View>(R.id.button_continue) as AppCompatButton
        buttonContinue.text = getString(R.string.Generic_Ok)


        viewModel.isThankYouPageVisible = true

    }

    override fun getViewModel(): SurveyViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[SurveyViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "SurveyThankYouFragment"
        fun newInstance() = SurveyThankYouFragment()

    }


}