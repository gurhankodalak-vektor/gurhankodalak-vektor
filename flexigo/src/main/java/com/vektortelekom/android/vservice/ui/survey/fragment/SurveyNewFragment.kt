package com.vektortelekom.android.vservice.ui.survey.fragment

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.SurveyFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.base.component.surveyview.SurveyItemViewData
import com.vektortelekom.android.vservice.ui.survey.SurveyViewModel
import com.vektortelekom.android.vservice.ui.survey.adapter.SurveyAdapter
import com.vektortelekom.android.vservice.ui.survey.adapter.SurveyItemListener
import com.vektortelekom.android.vservice.utils.fromHtml
import javax.inject.Inject

class SurveyNewFragment : BaseFragment<SurveyViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: SurveyViewModel

    lateinit var binding: SurveyFragmentBinding

    private var answerIdsList: MutableList<Int> = ArrayList()
    private var secondaryAnswerIdsList: MutableList<Int> = ArrayList()

    private var surveyAdapter: SurveyAdapter? = null
    private var gridLayoutManager: GridLayoutManager? = null
    val adapterList = arrayListOf<SurveyItemViewData>()

    private var surveySecondaryAdapter: SurveyAdapter? = null
    val secondaryAdapterList = arrayListOf<SurveyItemViewData>()

    private lateinit var chipGr: ChipGroup

    private var isLoadedChipsSecondary = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<SurveyFragmentBinding>(inflater, R.layout.survey_fragment, container, false).apply {
            lifecycleOwner = this@SurveyNewFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        answerIdsList = mutableListOf()
        secondaryAnswerIdsList = mutableListOf()

        if (viewModel.isSurveyFirstScreen) {
            binding.textviewQuestionText.text = getString(R.string.survey_welcome_title)
            binding.textviewDescription.text = getString(R.string.survey_welcome_description)
        } else {

            viewModel.secondaryAnswers.value = secondaryAnswerIdsList

            binding.textviewQuestionText.text = fromHtml(viewModel.surveyQuestion.value?.questionText)

            binding.textviewDescription.text = viewModel.surveyQuestion.value?.description

        }

        binding.addSecondaryDescription.setOnClickListener {

            if (!isLoadedChipsSecondary) {

                isLoadedChipsSecondary = true
                binding.textviewSecondaryDescription.visibility = View.VISIBLE
                binding.textviewSecondaryDescription.text = viewModel.surveyQuestion.value!!.secondaryDescription
                binding.addSecondaryDescription.text = getString(R.string.remove_secondary_mode)

                addSecondaryQuestion()
            } else {
                secondaryAdapterList.clear()
                isLoadedChipsSecondary = false
                binding.addSecondaryDescription.text = getString(R.string.add_secondary_mode)
                binding.textviewSecondaryDescription.visibility = View.GONE

                secondaryAnswerIdsList.clear()
                viewModel.secondaryAnswers.value = secondaryAnswerIdsList


            }

        }

        viewModel.surveyQuestion.value?.secondaryDescription?.let {
            binding.addSecondaryDescription.visibility = View.VISIBLE
        } ?: run {
            binding.addSecondaryDescription.visibility = View.GONE
            binding.surveySecondaryListView.visibility = View.GONE
        }

        viewModel.surveyQuestion.value?.answers?.let {
            addListItem(firstAnswerList)
        }
    }

    private fun addSecondaryQuestion() {
        binding.surveySecondaryListView.visibility = View.VISIBLE
        addSecondaryListItem(secondAnswerList)
    }

    private fun addSecondaryListItem(listener: SurveyItemListener){
        val filterAnswerCount = viewModel.surveyQuestion.value?.answers!!.filter { item ->
            item.answerText!!.length > 14
        }

        gridLayoutManager = if (filterAnswerCount.size > 1) {
            GridLayoutManager(this.requireContext(), 1)
        } else {
            GridLayoutManager(this.requireContext(), 2)
        }
        for (list in viewModel.surveyQuestion.value?.answers!!) {
            secondaryAdapterList.add(SurveyItemViewData(list.answerText, list.id, false))
        }
        surveySecondaryAdapter = SurveyAdapter(secondaryAdapterList, listener)
        binding.surveySecondaryListView.layoutManager = gridLayoutManager
        binding.surveySecondaryListView.adapter = surveySecondaryAdapter
    }

    private val secondAnswerList = object : SurveyItemListener{
        override fun onClickItem(data: SurveyItemViewData) {
            if (!(secondaryAnswerIdsList.size > 0 && data.surveyId == secondaryAnswerIdsList[0])){
                if (viewModel.isMultiSelectionEnabled.value == true) {

                    changeSecondaryItemList(data)

                    if (!data.isChecked) {
                        if (secondaryAnswerIdsList.find { it == data.surveyId } == null) {
                            secondaryAnswerIdsList.add(data.surveyId ?: 0)
                        }
                    } else {
                        if (secondaryAnswerIdsList.size > 1) {
                            secondaryAnswerIdsList.remove(data.surveyId)
                        }
                    }
                    viewModel.secondaryAnswers.value = secondaryAnswerIdsList
                } else {

                    changeSecondaryItemList(data, true)

                    secondaryAnswerIdsList.clear()
                    secondaryAnswerIdsList.add(data.surveyId ?: 0)
                    viewModel.secondaryAnswers.value = secondaryAnswerIdsList
                }
            }
        }
    }

    private fun changeItemList(adapter: SurveyAdapter?,adapterList: ArrayList<SurveyItemViewData>,data: SurveyItemViewData, isClearAllItem: Boolean = false) {
        var selectedItemIndex = 0
        adapterList.forEachIndexed { index, surveyItem ->
            if (surveyItem == data) {
                selectedItemIndex = index
            }
        }
        if (isClearAllItem) {
            val tempList = arrayListOf<SurveyItemViewData>()
            adapterList.forEach { surveyItem ->
                tempList.add(SurveyItemViewData(surveyItem.surveyText, surveyItem.surveyId))
            }
            adapterList.clear()
            adapterList.addAll(tempList)
        }
        val changeItem = SurveyItemViewData(
                data.surveyText,
                data.surveyId,
                !data.isChecked
        )

        val removeItem = adapterList.find { listItem -> listItem.surveyId == data.surveyId }
        adapterList.remove(removeItem)
        adapterList.add(selectedItemIndex, changeItem)
        adapter?.setList(adapterList)
        adapter?.notifyDataSetChanged()
    }

    private fun changeSecondaryItemList(data: SurveyItemViewData, isClearAllItem: Boolean = false) {
        var selectedItemIndex = 0
        secondaryAdapterList.forEachIndexed { index, surveyItem ->
            if (surveyItem == data) {
                selectedItemIndex = index
            }
        }
        if (isClearAllItem) {
            val tempList = arrayListOf<SurveyItemViewData>()
            secondaryAdapterList.forEach { surveyItem ->
                tempList.add(SurveyItemViewData(surveyItem.surveyText, surveyItem.surveyId))
            }
            secondaryAdapterList.clear()
            secondaryAdapterList.addAll(tempList)
        }
        val changeItem = SurveyItemViewData(
                data.surveyText,
                data.surveyId,
                !data.isChecked
        )

        val removeItem = secondaryAdapterList.find { listItem -> listItem.surveyId == data.surveyId }
        secondaryAdapterList.remove(removeItem)
        secondaryAdapterList.add(selectedItemIndex, changeItem)
        surveySecondaryAdapter?.setList(secondaryAdapterList)
        surveySecondaryAdapter?.notifyDataSetChanged()
    }

    private fun addListItem(listener: SurveyItemListener) {
        val filterAnswerCount = viewModel.surveyQuestion.value?.answers!!.filter { item ->
            item.answerText!!.length > 14
        }

        gridLayoutManager = if (filterAnswerCount.size > 1) {
            GridLayoutManager(this.requireContext(), 1)
        } else {
            GridLayoutManager(this.requireContext(), 2)
        }
        for (list in viewModel.surveyQuestion.value?.answers!!) {
            adapterList.add(SurveyItemViewData(list.answerText, list.id, false))
        }
        surveyAdapter = SurveyAdapter(adapterList, listener)
        binding.surveyListView.layoutManager = gridLayoutManager
        binding.surveyListView.adapter = surveyAdapter
    }

    private val firstAnswerList = object : SurveyItemListener{
        override fun onClickItem(data: SurveyItemViewData) {
            if (!(answerIdsList.size > 0 && data.surveyId == answerIdsList[0])){
                if (viewModel.isMultiSelectionEnabled.value == true) {

                    changeItemList(data)

                    if (!data.isChecked) {
                        if (answerIdsList.find { it == data.surveyId } == null) {
                            answerIdsList.add(data.surveyId ?: 0)
                        }
                    } else {
                        if (answerIdsList.size > 1) {
                            answerIdsList.remove(data.surveyId)
                        }
                    }
                    viewModel.selectedAnswers.value = answerIdsList
                    viewModel.selectedAnswers.value?.size?.let { listSize ->
                        if (listSize > 0) {
                            viewModel.isContinueButtonEnabled.value = true
                        }
                    }
                } else {

                    changeItemList(data, true)

                    answerIdsList.clear()
                    answerIdsList.add(data.surveyId ?: 0)
                    viewModel.selectedAnswers.value = answerIdsList
                    viewModel.selectedAnswers.value?.size?.let { listSize ->
                        if (listSize > 0) {
                            viewModel.isContinueButtonEnabled.value = true
                        }
                    }
                }
            }
        }
    }

    //TODO umut tek method için refactor yapılacak
    private fun changeItemList(data: SurveyItemViewData, isClearAllItem: Boolean = false) {
        var selectedItemIndex = 0
        adapterList.forEachIndexed { index, surveyItem ->
            if (surveyItem == data) {
                selectedItemIndex = index
            }
        }
        if (isClearAllItem) {
            val tempList = arrayListOf<SurveyItemViewData>()
            adapterList.forEach { surveyItem ->
                tempList.add(SurveyItemViewData(surveyItem.surveyText, surveyItem.surveyId))
            }
            adapterList.clear()
            adapterList.addAll(tempList)
        }
        val changeItem = SurveyItemViewData(
                data.surveyText,
                data.surveyId,
                !data.isChecked
        )

        val removeItem = adapterList.find { listItem -> listItem.surveyId == data.surveyId }
        adapterList.remove(removeItem)
        adapterList.add(selectedItemIndex, changeItem)
        surveyAdapter?.setList(adapterList)
        surveyAdapter?.notifyDataSetChanged()
    }

    override fun getViewModel(): SurveyViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory).get(SurveyViewModel::class.java) }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "SurveyNewFragment"
        fun newInstance() = SurveyNewFragment()

    }
}