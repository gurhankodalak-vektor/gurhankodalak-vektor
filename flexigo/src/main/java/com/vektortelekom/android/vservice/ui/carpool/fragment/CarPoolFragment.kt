package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CarpoolFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolAdapter
import javax.inject.Inject


class CarPoolFragment : BaseFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolFragmentBinding

    var adapter: CarPoolAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolFragmentBinding>(inflater, R.layout.carpool_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolFragment
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CarPoolAdapter { route ->

        }
        binding.recyclerview.adapter = adapter

    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CarPoolFragment"
        fun newInstance() = CarPoolFragment()
    }


}