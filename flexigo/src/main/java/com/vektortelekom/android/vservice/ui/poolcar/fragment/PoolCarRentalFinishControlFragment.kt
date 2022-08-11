package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarRentalFinishControlFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import javax.inject.Inject

class PoolCarRentalFinishControlFragment: BaseFragment<PoolCarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    lateinit var binding: PoolCarRentalFinishControlFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarRentalFinishControlFragmentBinding>(inflater, R.layout.pool_car_rental_finish_control_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarRentalFinishControlFragment
            viewModel = this@PoolCarRentalFinishControlFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonContinue.isEnabled = BuildConfig.FLAVOR == "tums"

        if(BuildConfig.FLAVOR != "tums") {
            binding.checkBox1.setOnCheckedChangeListener { _, isChecked ->

                binding.buttonContinue.isEnabled = isChecked && binding.checkBox2.isChecked && binding.checkBox3.isChecked && binding.checkBoxConfirm.isChecked

            }

            binding.checkBox2.setOnCheckedChangeListener { _, isChecked ->

                binding.buttonContinue.isEnabled = isChecked && binding.checkBox1.isChecked && binding.checkBox3.isChecked && binding.checkBoxConfirm.isChecked

            }

            binding.checkBox3.setOnCheckedChangeListener { _, isChecked ->

                binding.buttonContinue.isEnabled = isChecked && binding.checkBox1.isChecked && binding.checkBox2.isChecked && binding.checkBoxConfirm.isChecked

            }

            binding.checkBoxConfirm.setOnCheckedChangeListener { _, isChecked ->

                binding.buttonContinue.isEnabled = isChecked && binding.checkBox1.isChecked && binding.checkBox2.isChecked && binding.checkBox3.isChecked

            }
        }

    }

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarRentalFinishControlFragment"

        fun newInstance() = PoolCarRentalFinishControlFragment()

    }

}