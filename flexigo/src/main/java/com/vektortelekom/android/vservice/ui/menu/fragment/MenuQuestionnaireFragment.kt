package com.vektortelekom.android.vservice.ui.menu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektor.vshare_api_ktx.model.MultiRatingRequest
import com.vektor.vshare_api_ktx.model.RatingModel
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.MenuQuestionnaireFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.menu.MenuViewModel
import com.vektortelekom.android.vservice.utils.AppConstants
import javax.inject.Inject
import kotlin.math.min

class MenuQuestionnaireFragment : BaseFragment<MenuViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    lateinit var binding: MenuQuestionnaireFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<MenuQuestionnaireFragmentBinding>(inflater, R.layout.menu_questionnaire_fragment, container, false).apply {
            lifecycleOwner = this@MenuQuestionnaireFragment
            viewModel = this@MenuQuestionnaireFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rating1 = RatingModel((0).toLong(), rating = "0", type = AppConstants.RatingType2.DRIVER)
        val rating2 = RatingModel((0).toLong(), rating = "0", type = AppConstants.RatingType2.VEHICLE)
        val rating3 = RatingModel((0).toLong(), rating = "0", type = AppConstants.RatingType2.OVERALL)

        val imageSelected = ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_selected)
        val imageUnselected = ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_unselected)

        val click1 = View.OnClickListener { view ->
            when (view.id) {
                R.id.img_1_1 -> rating1.rating = "1"
                R.id.img_1_2 -> rating1.rating = "2"
                R.id.img_1_3 -> rating1.rating = "3"
                R.id.img_1_4 -> rating1.rating = "4"
                R.id.img_1_5 -> rating1.rating = "5"
            }

            val level = rating1.rating?.toInt() ?: 0

            binding.img11.setImageDrawable(if (level >= 1) imageSelected else imageUnselected)
            binding.img12.setImageDrawable(if (level >= 2) imageSelected else imageUnselected)
            binding.img13.setImageDrawable(if (level >= 3) imageSelected else imageUnselected)
            binding.img14.setImageDrawable(if (level >= 4) imageSelected else imageUnselected)
            binding.img15.setImageDrawable(if (level >= 5) imageSelected else imageUnselected)
        }

        val click2 = View.OnClickListener { view ->
            when (view.id) {
                R.id.img_2_1 -> rating2.rating = "1"
                R.id.img_2_2 -> rating2.rating = "2"
                R.id.img_2_3 -> rating2.rating = "3"
                R.id.img_2_4 -> rating2.rating = "4"
                R.id.img_2_5 -> rating2.rating = "5"
            }

            val level = rating2.rating?.toInt() ?: 0

            binding.img21.setImageDrawable(if (level >= 1) imageSelected else imageUnselected)
            binding.img22.setImageDrawable(if (level >= 2) imageSelected else imageUnselected)
            binding.img23.setImageDrawable(if (level >= 3) imageSelected else imageUnselected)
            binding.img24.setImageDrawable(if (level >= 4) imageSelected else imageUnselected)
            binding.img25.setImageDrawable(if (level >= 5) imageSelected else imageUnselected)
        }

        val click3 = View.OnClickListener { view ->
            when (view.id) {
                R.id.img_3_1 -> rating3.rating = "1"
                R.id.img_3_2 -> rating3.rating = "2"
                R.id.img_3_3 -> rating3.rating = "3"
                R.id.img_3_4 -> rating3.rating = "4"
                R.id.img_3_5 -> rating3.rating = "5"
            }

            val level = rating3.rating?.toInt() ?: 0

            binding.img31.setImageDrawable(if (level >= 1) imageSelected else imageUnselected)
            binding.img32.setImageDrawable(if (level >= 2) imageSelected else imageUnselected)
            binding.img33.setImageDrawable(if (level >= 3) imageSelected else imageUnselected)
            binding.img34.setImageDrawable(if (level >= 4) imageSelected else imageUnselected)
            binding.img35.setImageDrawable(if (level >= 5) imageSelected else imageUnselected)
        }

        binding.img11.setOnClickListener(click1)
        binding.img12.setOnClickListener(click1)
        binding.img13.setOnClickListener(click1)
        binding.img14.setOnClickListener(click1)
        binding.img15.setOnClickListener(click1)

        binding.img21.setOnClickListener(click2)
        binding.img22.setOnClickListener(click2)
        binding.img23.setOnClickListener(click2)
        binding.img24.setOnClickListener(click2)
        binding.img25.setOnClickListener(click2)

        binding.img31.setOnClickListener(click3)
        binding.img32.setOnClickListener(click3)
        binding.img33.setOnClickListener(click3)
        binding.img34.setOnClickListener(click3)
        binding.img35.setOnClickListener(click3)

        binding.buttonSubmit.setOnClickListener {
            var request: MultiRatingRequest? = null

            var text = binding.editTextDescription.text?.trim()?:""
            text = text.substring(0, min(text.length, 5000))

            val ratings: MutableList<RatingModel> = mutableListOf()
            if (rating1.rating != "0") {
                rating1.description = text
                ratings.add(rating1)
            }
            if (rating2.rating != "0") {
                rating2.description = text
                ratings.add(rating2)
            }
            if (rating3.rating != "0") {
                rating3.description = text
                ratings.add(rating3)
            }

            if (ratings.size == 3) {
                request = MultiRatingRequest(ratings)
                viewModel.sendMultiRating(request)
            } else {
                val dialog = AppDialog.Builder(requireContext())
                        .setIconVisibility(true)
                        .setTitle(requireContext().getString(R.string.generic_error_title))
                        .setSubtitle(requireContext().getString(R.string.questionnaire_rate_it))
                        .setOkButton(requireContext().resources.getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                        }
                        .create()
                dialog.show()
            }

        }

    }

    override fun getViewModel(): MenuViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[MenuViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "MenuQuestionnaireFragment"

        fun newInstance() = MenuQuestionnaireFragment()

    }

}