package com.vektortelekom.android.vservice.ui.registration.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupInstance
import com.vektortelekom.android.vservice.databinding.EmailCodeFragmentBinding
import com.vektortelekom.android.vservice.databinding.SelectCampusFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.calendar.dialog.CalendarSendDemandWorkgroupDialog
import com.vektortelekom.android.vservice.ui.registration.RegistrationViewModel
import com.vektortelekom.android.vservice.ui.registration.adapter.CampusAdapter
import com.vektortelekom.android.vservice.ui.shuttle.adapter.ShuttleDemandAdapter
import com.vektortelekom.android.vservice.ui.shuttle.adapter.ShuttleWorkgroupInstanceAdapter
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import javax.inject.Inject

class SelectCampusFragment : BaseFragment<RegistrationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: RegistrationViewModel

    lateinit var binding: SelectCampusFragmentBinding

    private var campusAdapter: CampusAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<SelectCampusFragmentBinding>(inflater, R.layout.select_campus_fragment, container, false).apply {
            lifecycleOwner = this@SelectCampusFragment
            viewModel = this@SelectCampusFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonContinue.setOnClickListener{
            NavHostFragment.findNavController(this).navigateUp()
        }


        campusAdapter = CampusAdapter(object : CampusAdapter.ItemClickListener {
            override fun onItemClicked(destinationModel: DestinationModel) {


            }
        })


    }

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