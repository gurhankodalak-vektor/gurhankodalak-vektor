package com.vektortelekom.android.vservice.ui.taxi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.TaxiListFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.taxi.TaxiViewModel
import com.vektortelekom.android.vservice.ui.taxi.adapter.TaxiListAdapter
import javax.inject.Inject

class TaxiListFragment: BaseFragment<TaxiViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: TaxiViewModel

    lateinit var binding: TaxiListFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<TaxiListFragmentBinding>(inflater, R.layout.taxi_list_fragment, container, false).apply {
            lifecycleOwner = this@TaxiListFragment
            viewModel = this@TaxiListFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getTaxiUsages()

        viewModel.taxiUsages.observe(viewLifecycleOwner) { taxiUsages ->
            binding.recyclerViewTaxiUsages.adapter = TaxiListAdapter(taxiUsages)
        }

    }

    override fun getViewModel(): TaxiViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[TaxiViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "TaxiListFragment"
        fun newInstance() = TaxiListFragment()
    }

}