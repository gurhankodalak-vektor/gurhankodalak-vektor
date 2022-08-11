package com.vektortelekom.android.vservice.ui.flexiride.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.FlexirideListFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.flexiride.FlexirideViewModel
import com.vektortelekom.android.vservice.ui.flexiride.adapter.FlexirideListAdapter
import com.vektortelekom.android.vservice.utils.dpToPx
import javax.inject.Inject

class FlexirideListFragment: BaseFragment<FlexirideViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: FlexirideViewModel

    lateinit var binding: FlexirideListFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<FlexirideListFragmentBinding>(inflater, R.layout.flexiride_list_fragment, container, false).apply {
            lifecycleOwner = this@FlexirideListFragment
            viewModel = this@FlexirideListFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getFlexirideList()

        viewModel.flexirideList.observe(viewLifecycleOwner) {

            val flexirideList = mutableListOf<PoolcarAndFlexirideModel>()

            var countAccepted = 0
            var countPending = 0
            var countRejected = 0
            for(flexiride in it) {
                when(flexiride.status) {
                    FlexirideAndPoolcarStatus.APPROVED, FlexirideAndPoolcarStatus.PLANNED -> {
                        countAccepted++
                        flexirideList.add(flexiride)
                    }
                    FlexirideAndPoolcarStatus.PENDING -> {
                        countPending++
                        flexirideList.add(flexiride)
                    }
                    FlexirideAndPoolcarStatus.REJECTED -> {
                        countRejected++
                        flexirideList.add(flexiride)
                    }
                    FlexirideAndPoolcarStatus.FINISHED -> {
                        countAccepted++
                        flexirideList.add(flexiride)
                    }
                    else -> {

                    }
                }
            }
            binding.textViewAcceptCount.text = countAccepted.toString()
            binding.textViewExpectantCount.text = countPending.toString()
            binding.textViewRejectedCount.text = countRejected.toString()


            binding.recyclerViewFlexirideList.adapter = FlexirideListAdapter(flexirideList, object:FlexirideListAdapter.FlexirideItemListener {
                override fun flexirideSelected(flexiride: PoolcarAndFlexirideModel) {

                    viewModel.startedFlexiride.value = flexiride

                    viewModel.navigator?.showFlexiridePlannedFragment(null)

                }

                override fun deleteFlexiride(flexiride: PoolcarAndFlexirideModel) {
                    viewModel.deleteFlexiride(flexiride.id?:0)
                }

                override fun evaluateFlexiride(flexiride: PoolcarAndFlexirideModel) {
                    viewModel.evaluateFlexiride = flexiride
                    viewModel.navigator?.showFlexirideSurveyFragment()
                }

            })
            binding.recyclerViewFlexirideList.addItemDecoration(getItemDecoration(it.size))
        }

    }

    private fun getItemDecoration(size: Int): RecyclerView.ItemDecoration {
        return object: RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)

                with(outRect) {
                    top = if (parent.getChildAdapterPosition(view) == 0) {
                        20f.dpToPx(requireContext())
                    } else {
                        5f.dpToPx(requireContext())
                    }
                    bottom = if (parent.getChildAdapterPosition(view) == size -1) {
                        20f.dpToPx(requireContext())
                    } else {
                        5f.dpToPx(requireContext())
                    }
                }
            }
        }
    }

    override fun getViewModel(): FlexirideViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[FlexirideViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "FlexirideListFragment"

        fun newInstance() = FlexirideListFragment()

    }

}