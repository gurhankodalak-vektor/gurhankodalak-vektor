package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.CarPoolListModel
import com.vektortelekom.android.vservice.data.model.ChooseRiderRequest
import com.vektortelekom.android.vservice.data.model.InfoUpdateRequest
import com.vektortelekom.android.vservice.databinding.CarpoolRiderFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolAdapter
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolMatchedAdapter
import com.vektortelekom.android.vservice.ui.carpool.dialog.CarPoolPhoneNumberDialog
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

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            }
            )

        adapter = CarPoolAdapter(object: CarPoolAdapter.CarPoolSwipeListener{
            override fun onDislikeSwipe(item: CarPoolListModel) {
                if (viewModel.isDriver.value == true){
                    val request = ChooseRiderRequest(item.id, false, null)
                    viewModel.setChooseRider(request, false)
                }

            }

            override fun onLikeSwipe(item: CarPoolListModel) {
                if (viewModel.isDriver.value == true){

                    if (AppDataManager.instance.personnelInfo?.phoneNumber == null || AppDataManager.instance.personnelInfo?.phoneNumber.equals("")){
                        showPhoneNumberDialog(item.id)
                    } else{
                        val request = ChooseRiderRequest(item.id, true, null)
                        viewModel.setChooseRider(request, false)
                    }

                }
            }

        })
        binding.recyclerviewRiders.adapter = adapter

        matchedAdapter = CarPoolMatchedAdapter("riders", object : CarPoolMatchedAdapter.CarPoolItemClickListener{
            override fun onCancelClicked(item: CarPoolListModel) {
                if (viewModel.isDriver.value == true){

                    (requireActivity() as BaseActivity<*>).showPd()

                    showEndPoolingConfirmation(item.id, item.name)
                }
            }

            override fun onApproveClicked(item: CarPoolListModel) {
                if (viewModel.isDriver.value == true){
                    (requireActivity() as BaseActivity<*>).showPd()

                    val request = ChooseRiderRequest(item.id, isMatchedState = true, driverApproved = true)
                    viewModel.setChooseRider(request, true)

                    showInformMatching(item.name)
                }
            }

            override fun onNavigateClicked(item: CarPoolListModel) {

            }

            override fun onCallClicked(item: CarPoolListModel) {
                val phoneNumber = item.phoneNumber

                if (phoneNumber == null)
                    viewModel.navigator?.handleError(Exception(getString(R.string.error_empty_phone_number)))
                else{
                    AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
                        .setTitle(getString(R.string.call_2))
                        .setMessage(getString(R.string.will_call, phoneNumber))
                        .setPositiveButton(getString(R.string.Generic_Ok)) { d, _ ->
                            d.dismiss()
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(phoneNumber)))
                            startActivity(intent)
                        }
                        .setNegativeButton(getString(R.string.cancel)) { d, _ ->
                            d.dismiss()
                        }
                        .create().show()
                }
            }

        })
        binding.recyclerviewMatchedRiders.adapter = matchedAdapter

        viewModel.isRider.observe(viewLifecycleOwner){
            if (it != null){
                adapter?.isOnlyReadMode(it)

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

        }

        viewModel.carPoolPreferences.observe(viewLifecycleOwner){
            if (it == null)
                adapter?.isOnlyReadMode(true)
            else {
                if (it.isRider == true)
                    adapter?.isOnlyReadMode(true)
                else
                    adapter?.isOnlyReadMode(false)
            }
        }

        viewModel.closeRiders.observe(viewLifecycleOwner){
            if (it != null && it.isNotEmpty()){
                binding.recyclerviewRiders.visibility = View.VISIBLE

                adapter?.setList(it)
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
            } else{

                binding.textviewMatchedRidersTitle.visibility = View.GONE
                binding.imageviewOrangeCircle.visibility = View.GONE
                binding.recyclerviewMatchedRiders.visibility = View.GONE

            }
        }

    }

    fun showPhoneNumberDialog(itemId: Long) {

        val phoneNumberDialog = CarPoolPhoneNumberDialog(itemId, "driver", object: CarPoolPhoneNumberDialog.PhoneNumberClickListener {
            override fun sendClick(phoneNumber: String) {
                val request = InfoUpdateRequest(phoneNumber)
                viewModel.sendPhoneNumber(request)
            }
        })

        val ft: FragmentTransaction = childFragmentManager.beginTransaction()
        val prev: Fragment? = childFragmentManager.findFragmentByTag("CarPoolPhoneNumberDialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        phoneNumberDialog.setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        phoneNumberDialog.show(ft, "CarPoolPhoneNumberDialog")

        childFragmentManager.executePendingTransactions()

    }

    private fun showEndPoolingConfirmation(driverPersonnelId: Long, driverName: String) {

        val dialog = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.setCancelable(false)
        dialog.setMessage(resources.getString(R.string.endpool_carpooling, driverName))
        dialog.setPositiveButton(resources.getString(R.string.confirm)) { d, _ ->

            val request = ChooseRiderRequest(driverPersonnelId, false, null)
            viewModel.setChooseRider(request, true)

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

//            (requireActivity() as BaseActivity<*>).dismissPd()
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