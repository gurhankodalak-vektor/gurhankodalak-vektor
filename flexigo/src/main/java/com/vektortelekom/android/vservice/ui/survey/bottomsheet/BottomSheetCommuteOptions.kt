package com.vektortelekom.android.vservice.ui.survey.bottomsheet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.BottomSheetCommuteOptionsBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.home.HomeActivity
import com.vektortelekom.android.vservice.ui.route.RouteSelectionActivity
import com.vektortelekom.android.vservice.ui.survey.SurveyViewModel
import com.vektortelekom.android.vservice.ui.survey.adapter.CommuteOptionsAdapter
import javax.inject.Inject

class BottomSheetCommuteOptions : BaseFragment<SurveyViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: SurveyViewModel

    lateinit var binding: BottomSheetCommuteOptionsBinding

    private var commuteOptionsAdapter: CommuteOptionsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<BottomSheetCommuteOptionsBinding>(
            inflater,
            R.layout.bottom_sheet_commute_options,
            container,
            false
        ).apply {
            lifecycleOwner = this@BottomSheetCommuteOptions
            viewModel = this@BottomSheetCommuteOptions.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity<*>).showPd()


        (requireActivity() as BaseActivity<*>).showPd()
        viewModel.getCommuteOptions()

        binding.textviewTitle.text = resources.getString(R.string.compare_commute_options)
        binding.textviewDescription.text =
            resources.getString(R.string.compare_commute_options_text)

        binding.imageViewClose.setOnClickListener {
            showHomeActivity(requireContext())
        }

        binding.recyclerViewCommuteOptions.addItemDecoration(
            DividerItemDecoration(context,
            DividerItemDecoration.VERTICAL)
        )

        viewModel.options.observe(viewLifecycleOwner){
            if (it != null){
                (requireActivity() as BaseActivity<*>).dismissPd()
                commuteOptionsAdapter?.setList(it)
                binding.recyclerViewCommuteOptions.adapter = commuteOptionsAdapter
            }
        }

        commuteOptionsAdapter = CommuteOptionsAdapter(object : CommuteOptionsAdapter.CommuteOptionsItemClickListener {
                override fun onItemClicked() {

                    if (AppDataManager.instance.personnelInfo?.workgroupInstanceId != null) {

                        val intent = Intent(requireContext(), RouteSelectionActivity::class.java)
                        intent.putExtra("isFromAddressSelect", true)
                        startActivity(intent)
                    } else {
                        activity?.finish()
                        val intent = Intent(requireContext(), HomeActivity::class.java)
                        startActivity(intent)
                    }

                }
            })

    }

    private fun showHomeActivity(context: Context) {
        val intent = Intent(context, HomeActivity::class.java)
        context.startActivity(intent)
    }

    override fun getViewModel(): SurveyViewModel {
        viewModel = activity?.run {
            ViewModelProvider(
                requireActivity(),
                factory
            )[SurveyViewModel::class.java]
        }
            ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "BottomSheetCommuteOptions"

        fun newInstance() = BottomSheetCommuteOptions()

    }

}