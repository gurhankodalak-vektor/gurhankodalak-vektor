package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.CarPoolListModel
import com.vektortelekom.android.vservice.data.model.ChooseDriverRequest
import com.vektortelekom.android.vservice.data.model.ChooseRiderRequest
import com.vektortelekom.android.vservice.data.model.PersonsModel
import com.vektortelekom.android.vservice.databinding.CarpoolMatchingFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolMatchedAdapter
import javax.inject.Inject

class CarPoolMatchingRiderFragment : BaseFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolMatchingFragmentBinding

    var matchedAdapter: CarPoolMatchedAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolMatchingFragmentBinding>(inflater, R.layout.carpool_matching_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolMatchingRiderFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textviewMatchedTitle.text = resources.getText(R.string.your_carpool_driver)

        matchedAdapter = CarPoolMatchedAdapter("riders_match", object : CarPoolMatchedAdapter.CarPoolItemClickListener{
            override fun onCancelClicked(item: CarPoolListModel) {
                if (viewModel.isRider.value == true){
                    showEndPoolingConfirmation(item.id, item.name)
                }
            }

            override fun onApproveClicked(item: CarPoolListModel) {

            }

            override fun onCallClicked(item: CarPoolListModel) {

            }

        })

        viewModel.ridingWith.observe(viewLifecycleOwner){
            if (it != null) {
                list.add(0, it)
                matchedAdapter?.setList(listOf(it))
                binding.recyclerviewMatchedRiders.adapter = matchedAdapter
            } else
                activity?.finish()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            }
            )

    }

    private fun showEndPoolingConfirmation(driverPersonnelId: Long, driverName: String) {

        val dialog = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.setCancelable(false)
        dialog.setMessage(resources.getString(R.string.endpool_carpooling, driverName))
        dialog.setPositiveButton(resources.getString(R.string.confirm)) { d, _ ->

            (requireActivity() as BaseActivity<*>).showPd()
            val request = ChooseDriverRequest(driverPersonnelId, false)
            viewModel.setChooseDriver(request, true)

            d.dismiss()
        }
        dialog.setNegativeButton(resources.getString(R.string.cancel)) { d, _ ->
            d.dismiss()
        }

        dialog.show()

    }

    var list: MutableList<CarPoolListModel> = mutableListOf()

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CarPoolMatchingRiderFragment"
        fun newInstance() = CarPoolMatchingRiderFragment()
    }


}