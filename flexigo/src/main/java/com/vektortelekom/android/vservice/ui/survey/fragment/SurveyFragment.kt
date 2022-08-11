package com.vektortelekom.android.vservice.ui.survey.fragment

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.SurveyFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.survey.SurveyViewModel
import com.vektortelekom.android.vservice.utils.fromHtml
import javax.inject.Inject


class SurveyFragment: BaseFragment<SurveyViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: SurveyViewModel

    lateinit var binding: SurveyFragmentBinding

    private var answerIdsList: MutableList<Int> = ArrayList()
    private var secondaryAnswerIdsList: MutableList<Int> = ArrayList()

    private lateinit var chipGr: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<SurveyFragmentBinding>(inflater, R.layout.survey_fragment, container, false).apply {
            lifecycleOwner = this@SurveyFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        answerIdsList = mutableListOf()
        secondaryAnswerIdsList = mutableListOf()

        if (viewModel.isSurveyFirstScreen){
            binding.textviewQuestionText.text = getString(R.string.survey_welcome_title)
           // val styledText: CharSequence = Html.fromHtml(getString(R.string.survey_welcome_description))
            binding.textviewDescription.text = getString(R.string.survey_welcome_description)
        } else{

            viewModel.secondaryAnswers.value = secondaryAnswerIdsList

            binding.chipGroup.isSingleSelection = true
            binding.chipGroup.isSelectionRequired = true

            binding.textviewQuestionText.text = fromHtml(viewModel.surveyQuestion.value?.questionText?.replace("commuting","commute"))

            binding.textviewDescription.text = viewModel.surveyQuestion.value?.description

        }

        binding.addSecondaryDescription.setOnClickListener {
            viewModel.isSecondaryAnswerEnabled.value = viewModel.isSecondaryAnswerEnabled.value != true
        }

        viewModel.surveyQuestion.value?.secondaryDescription?.let {
            binding.addSecondaryDescription.visibility = View.VISIBLE
        } ?: run {
            binding.addSecondaryDescription.visibility = View.GONE
        }

        viewModel.surveyQuestion.value?.answers?.let {
            addChipToGroup(binding.chipGroup)
        }

        viewModel.isSecondaryAnswerEnabled.observe(requireActivity()) {
            if (it != null) {
                if (it == true) {
                    binding.textviewSecondaryDescription.visibility = View.VISIBLE
                    binding.textviewSecondaryDescription.text = viewModel.surveyQuestion.value!!.secondaryDescription
                    binding.addSecondaryDescription.text = getString(R.string.remove_secondary_mode)
                    addSecondaryQuestion()
                } else {
                    binding.addSecondaryDescription.text = getString(R.string.add_secondary_mode)
                    binding.textviewSecondaryDescription.visibility = View.GONE

                    secondaryAnswerIdsList.clear()
                    viewModel.secondaryAnswers.value = secondaryAnswerIdsList

                    binding.layout.removeView(chipGr)
                }
            }
        }

        viewModel.isReloadFragment.observe(requireActivity()) {
            if (it != null) {
                if (it == true) {
                    try {
                        if (chipGr.childCount > 0) {
                            binding.layout.removeView(chipGr)
                        }
                    } catch (e: Exception) {
                        //nothing
                    }

                }
            }
        }

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)
            chip?.let {chipView ->

                answerIdsList.clear()
                answerIdsList.add(chipView.tag as Int)
                viewModel.selectedAnswers.value = answerIdsList

            } ?: kotlin.run {
            }
            if (viewModel.selectedAnswers.value?.size!! > 0)
                viewModel.isContinueButtonEnabled.value = true
        }

    }

    private fun addSecondaryQuestion(){
        chipGr = ChipGroup(context)

        val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, // width
                ConstraintLayout.LayoutParams.WRAP_CONTENT // height
        )

        chipGr.layoutParams = params
        params.topMargin  = 15
        chipGr.id = View.generateViewId()
        chipGr.isSingleSelection = true
        chipGr.isSelectionRequired = true

        binding.layout.addView(chipGr)

        chipGr.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)
            chip?.let {chipView ->

                    secondaryAnswerIdsList.clear()
                    secondaryAnswerIdsList.add(chipView.tag as Int)
                    viewModel.secondaryAnswers.value = secondaryAnswerIdsList

            } ?: kotlin.run {
            }

        }

        addChipToGroup(chipGr)

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.layout)

        constraintSet.connect(
                chipGr.id,
                ConstraintSet.TOP,
                binding.textviewSecondaryDescription.id,
                ConstraintSet.BOTTOM,
                15.toDp(requireContext())
        )

        constraintSet.connect(
                chipGr.id,
                ConstraintSet.START,
                R.id.layout,
                ConstraintSet.START,
                16.toDp(requireContext())
        )

        constraintSet.connect(
                chipGr.id,
                ConstraintSet.END,
                R.id.layout,
                ConstraintSet.END,
                16.toDp(requireContext())
        )

        constraintSet.applyTo(binding.layout)

    }

    private fun Int.toDp(context: Context):Int = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
    ).toInt()

    private fun addChipToGroup(group: ChipGroup){

            for (list in viewModel.surveyQuestion.value?.answers!!){
                val chip = layoutInflater.inflate(R.layout.chip, requireView().parent.parent as ViewGroup, false) as Chip
                chip.text = list.answerText
                chip.id = View.generateViewId()
                chip.isClickable = true
                chip.isCheckable = true
                chip.isChipIconVisible = false
                chip.isCheckedIconVisible = false
                chip.tag = list.id


                group.addView(chip)
            }

    }

    override fun getViewModel(): SurveyViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[SurveyViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "SurveyFragment"

        fun newInstance() = SurveyFragment()

    }
}