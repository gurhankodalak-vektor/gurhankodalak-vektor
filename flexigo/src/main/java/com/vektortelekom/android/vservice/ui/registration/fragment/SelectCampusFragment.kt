package com.vektortelekom.android.vservice.ui.registration.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.UpdatePersonnelCampusRequest
import com.vektortelekom.android.vservice.databinding.SelectCampusFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.home.HomeActivity
import com.vektortelekom.android.vservice.ui.registration.RegistrationViewModel
import com.vektortelekom.android.vservice.ui.survey.SurveyActivity
import javax.inject.Inject

class SelectCampusFragment : BaseFragment<RegistrationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    lateinit var binding: SelectCampusFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<SelectCampusFragmentBinding>(inflater, R.layout.select_campus_fragment, container, false).apply {
            lifecycleOwner = this@SelectCampusFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.getDestinations()
                viewModel.destinationId.value = 0L
            }
        })
        binding.buttonContinue.isEnabled = false

        viewModel.getDestinations()


        binding.buttonContinue.setOnClickListener{
            viewModel.destinationId.value?.let { it1 ->
                val request = UpdatePersonnelCampusRequest(it1)
                viewModel.destinationsUpdate(request)
            }
        }

        viewModel.isCampusUpdateSuccess.observe(viewLifecycleOwner){
            if (it != null && it == true){
                if(viewModel.surveyQuestionId.value != null){
                    activity?.finish()
                    val intent = Intent(requireActivity(), SurveyActivity::class.java)
                    intent.putExtra("surveyQuestionId", viewModel.surveyQuestionId.value)
                    startActivity(intent)
                } else{
                    activity?.finish()
                    val intent = Intent(requireActivity(), HomeActivity::class.java)
                    intent.putExtra("is_coming_registration", true)
                    startActivity(intent)
                }

            }

        }

        viewModel.destinationId.observe(viewLifecycleOwner){
            binding.buttonContinue.isEnabled = it != null && it != 0L
        }

        viewModel.destinations.observe(viewLifecycleOwner){
            addChipToGroup(binding.chipGroup)
        }

        addChipToGroup(binding.chipGroup)

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)
            chip?.let {chipView ->
                viewModel.destinationId.value = chipView.tag as Long?
            } ?: kotlin.run {
            }
            
        }
    }

    private fun addChipToGroup(group: ChipGroup){
        binding.chipGroup.removeAllViews()

        viewModel.destinations.value?.let {

            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, // width
                ConstraintLayout.LayoutParams.WRAP_CONTENT // height
            )

            binding.chipGroup.layoutParams = params
            params.topMargin  = 15

            for (list in viewModel.destinations.value!!){
                val chip = layoutInflater.inflate(R.layout.chip, requireView().parent.parent as ViewGroup, false) as Chip
                chip.text = list.title
                chip.id = View.generateViewId()
                chip.isClickable = true
                chip.isCheckable = true
                chip.isChipIconVisible = false
                chip.isCheckedIconVisible = false
                chip.tag = list.id
                
                group.addView(chip)
            }


            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.layout)

            constraintSet.connect(
                binding.chipGroup.id,
                ConstraintSet.TOP,
                binding.textWelcomeHint.id,
                ConstraintSet.BOTTOM,
                15.toDp(requireContext())
            )

            constraintSet.connect(
                binding.chipGroup.id,
                ConstraintSet.START,
                R.id.layout,
                ConstraintSet.START,
                0.toDp(requireContext())
            )

            constraintSet.connect(
                binding.chipGroup.id,
                ConstraintSet.END,
                R.id.layout,
                ConstraintSet.END,
                0.toDp(requireContext())
            )

            constraintSet.applyTo(binding.layout)
        }


    }

    private fun Int.toDp(context: Context):Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
    ).toInt()

    override fun getViewModel(): RegistrationViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[RegistrationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "SelectCampusFragment"
        fun newInstance() = SelectCampusFragment()
    }


}