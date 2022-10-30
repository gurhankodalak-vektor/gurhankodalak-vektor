package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.CarPoolListModel
import com.vektortelekom.android.vservice.data.model.ChooseRiderRequest
import com.vektortelekom.android.vservice.databinding.CarpoolRiderFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolAdapter
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolMatchedAdapter
import javax.inject.Inject

class CarPoolRiderFragment : BaseFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolRiderFragmentBinding

    var adapter: CarPoolAdapter? = null
    var matchedAdapter: CarPoolMatchedAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolRiderFragmentBinding>(inflater, R.layout.carpool_rider_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolRiderFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CarPoolAdapter("riders", object: CarPoolAdapter.CarPoolSwipeListener{
            override fun onDislikeSwipe(item: CarPoolListModel) {
                if (viewModel.isDriver.value == true){
                    val request = ChooseRiderRequest(item.id, false, null)
                    viewModel.setChooseRider(request)
                }

            }

            override fun onLikeSwipe(item: CarPoolListModel) {
                if (viewModel.isDriver.value == true){
                    val request = ChooseRiderRequest(item.id, true, null)
                    viewModel.setChooseRider(request)
                }
            }

        })


        matchedAdapter = CarPoolMatchedAdapter("riders", object : CarPoolMatchedAdapter.CarPoolItemClickListener{
            override fun onCancelClicked(item: CarPoolListModel) {
                if (viewModel.isDriver.value == true){
                    showEndPoolingConfirmation(item.id, item.name)
                }
            }

            override fun onApproveClicked(item: CarPoolListModel) {
                if (viewModel.isDriver.value == true){
                    val request = ChooseRiderRequest(item.id, null, true)
                    viewModel.setChooseRider(request)
                    showInformMatching(item.name)
                }
            }

            override fun onCallClicked(item: CarPoolListModel) {

            }

        })

        viewModel.isRider.observe(viewLifecycleOwner){
            adapter?.setIsRider(it)

            if ((viewModel.closeRiders.value == null || viewModel.closeRiders.value!!.isEmpty())
                && (viewModel.matchedRiders.value == null || viewModel.matchedRiders.value!!.isEmpty())){
                    binding.layoutEmpty.visibility = View.VISIBLE
            } else
            {
                binding.layoutEmpty.visibility = View.GONE
            }

            if (viewModel.closeRiders.value!!.isNotEmpty() && viewModel.matchedRiders.value!!.isNotEmpty())
                binding.textviewRidersTitle.visibility = View.VISIBLE
            else
                binding.textviewRidersTitle.visibility = View.GONE
        }

        viewModel.carPoolPreferences.observe(viewLifecycleOwner){
            if (it == null)
                adapter?.isOnlyReadMode(true)
            else
                adapter?.isOnlyReadMode(false)
        }

        viewModel.closeRiders.observe(viewLifecycleOwner){
            if (it != null && it.isNotEmpty()){
                binding.recyclerviewRiders.visibility = View.VISIBLE

                adapter?.setList(it)
                binding.recyclerviewRiders.adapter = adapter
            } else{
                binding.recyclerviewRiders.visibility = View.GONE

            }
        }

        viewModel.matchedRiders.observe(viewLifecycleOwner){
            if (it != null && it.isNotEmpty()){

                binding.textviewMatchedRidersTitle.visibility = View.VISIBLE
                binding.imageviewOrangeCircle.visibility = View.VISIBLE
                binding.recyclerviewMatchedRiders.visibility = View.VISIBLE

                matchedAdapter?.setList(it)
                binding.recyclerviewMatchedRiders.adapter = matchedAdapter
            } else{

                binding.textviewMatchedRidersTitle.visibility = View.GONE
                binding.imageviewOrangeCircle.visibility = View.GONE
                binding.recyclerviewMatchedRiders.visibility = View.GONE

            }
        }

    }

    private fun showEndPoolingConfirmation(driverPersonnelId: Long, driverName: String) {

        val dialog = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.setCancelable(false)
        dialog.setMessage(resources.getString(R.string.endpool_carpooling, driverName))
        dialog.setPositiveButton(resources.getString(R.string.confirm)) { d, _ ->

            val request = ChooseRiderRequest(driverPersonnelId, false, null)
            viewModel.setChooseRider(request)

            d.dismiss()
        }
        dialog.setNegativeButton(resources.getString(R.string.cancel)) { d, _ ->
            d.dismiss()
        }

        dialog.show()

    }

    private fun showInformMatching(driverName: String) {

        val dialog = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.setCancelable(false)
        dialog.setTitle(resources.getString(R.string.great))
        dialog.setMessage(resources.getString(R.string.inform_matching, driverName))
        dialog.setPositiveButton(resources.getString(R.string.Generic_Ok)) { d, _ ->

            d.dismiss()
        }

        dialog.show()

    }

    override fun getViewModel(): CarPoolViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CarPoolViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CarPoolRiderFragment"
        fun newInstance() = CarPoolRiderFragment()
    }


}