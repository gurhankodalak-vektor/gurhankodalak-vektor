package com.vektortelekom.android.vservice.ui.pastuses.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.PastUseModel
import com.vektortelekom.android.vservice.data.model.PastUseType
import com.vektortelekom.android.vservice.data.model.TaxiLocationModel
import com.vektortelekom.android.vservice.databinding.PastUsesMainFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.pastuses.PastUsesViewModel
import com.vektortelekom.android.vservice.ui.pastuses.adapter.PastUsesListAdapter
import com.vektortelekom.android.vservice.utils.dpToPx
import javax.inject.Inject

class PastUsesMainFragment: BaseFragment<PastUsesViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PastUsesViewModel

    lateinit var binding: PastUsesMainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PastUsesMainFragmentBinding>(inflater, R.layout.past_uses_main_fragment, container, false).apply {
            lifecycleOwner = this@PastUsesMainFragment
            viewModel = this@PastUsesMainFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pastUses = mutableListOf<PastUseModel>()

        pastUses.add(
                PastUseModel(
                        id = 5,
                        title = "Some Title 1",
                        type = PastUseType.POOLCAR,
                        date = "19.01.2021",
                        duration = 50,
                        startLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Maslak"
                        ),
                        endLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Kadıköy"
                        )
                )
        )

        pastUses.add(
                PastUseModel(
                        id = 5,
                        title = "Some Title 2",
                        type = PastUseType.FLEXIRIDE,
                        date = "15.01.2021",
                        duration = 42,
                        startLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Maslak"
                        ),
                        endLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Kadıköy"
                        )
                )
        )

        pastUses.add(
                PastUseModel(
                        id = 5,
                        title = "Some Title 3",
                        type = PastUseType.POOLCAR,
                        date = "12.01.2021",
                        duration = 27,
                        startLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Maslak"
                        ),
                        endLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Kadıköy"
                        )
                )
        )

        pastUses.add(
                PastUseModel(
                        id = 5,
                        title = "Some Title 4",
                        type = PastUseType.TAXI,
                        date = "03.01.2021",
                        duration = 36,
                        startLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Maslak"
                        ),
                        endLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Kadıköy"
                        )
                )
        )

        pastUses.add(
                PastUseModel(
                        id = 5,
                        title = "Some Title 5",
                        type = PastUseType.FLEXIRIDE,
                        date = "07.12.2020",
                        duration = 29,
                        startLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Maslak"
                        ),
                        endLocation = TaxiLocationModel(
                                30.0,
                                20.0,
                                "Kadıköy"
                        )
                )
        )

        binding.recyclerViewPastUses.adapter = PastUsesListAdapter(pastUses)
        binding.recyclerViewPastUses.addItemDecoration(getItemDecoration(pastUses.size))

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

    override fun getViewModel(): PastUsesViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PastUsesViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PastUsesMainFragment"
        fun newInstance() = PastUsesMainFragment()
    }

}