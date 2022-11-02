package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.CarPoolListModel
import com.vektortelekom.android.vservice.data.model.ChooseDriverRequest
import com.vektortelekom.android.vservice.data.model.InfoUpdateRequest
import com.vektortelekom.android.vservice.databinding.CarpoolDriverFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolAdapter
import com.vektortelekom.android.vservice.ui.carpool.adapter.CarPoolMatchedAdapter
import com.vektortelekom.android.vservice.ui.carpool.dialog.CarPoolPhoneNumberDialog
import jp.wasabeef.recyclerview.animators.ScaleInTopAnimator
import javax.inject.Inject

class CarPoolDriverFragment : BaseFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolDriverFragmentBinding

    var adapter: CarPoolAdapter? = null
    var matchedAdapter: CarPoolMatchedAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolDriverFragmentBinding>(inflater, R.layout.carpool_driver_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolDriverFragment
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CarPoolAdapter("drivers", object: CarPoolAdapter.CarPoolSwipeListener{
            override fun onDislikeSwipe(item: CarPoolListModel) {
                if (viewModel.isRider.value == true){
                    val request = ChooseDriverRequest(item.id, false)
                    viewModel.setChooseDriver(request)
                }

            }

            override fun onLikeSwipe(item: CarPoolListModel) {
                if (viewModel.isRider.value == true){

                    if (AppDataManager.instance.personnelInfo?.phoneNumber == null || AppDataManager.instance.personnelInfo?.phoneNumber.equals("")){
                        showPhoneNumberDialog(item.id)
                    } else{
                        val request = ChooseDriverRequest(item.id, true)
                        viewModel.setChooseDriver(request)
                    }

                }
            }

        })

        matchedAdapter = CarPoolMatchedAdapter("drivers", object : CarPoolMatchedAdapter.CarPoolItemClickListener{
            override fun onCancelClicked(item: CarPoolListModel) {
              if (viewModel.isRider.value == true){
                  showEndPoolingConfirmation(item.id)
              }
            }

            override fun onApproveClicked(item: CarPoolListModel) {
            }

            override fun onCallClicked(item: CarPoolListModel) {

            }

        })

        viewModel.isDriver.observe(viewLifecycleOwner){
            if (it != null){
                adapter?.setIsDriver(it)

                if ((viewModel.closeDrivers.value == null || viewModel.closeDrivers.value!!.isEmpty())
                    && (viewModel.matchedDrivers.value == null || viewModel.matchedDrivers.value!!.isEmpty())){
                    binding.layoutEmpty.visibility = View.VISIBLE
                } else
                {
                    binding.layoutEmpty.visibility = View.GONE
                }

                if (viewModel.closeDrivers.value!!.isNotEmpty() && viewModel.matchedDrivers.value!!.isNotEmpty())
                    binding.textviewDriversTitle.visibility = View.VISIBLE
                else
                    binding.textviewDriversTitle.visibility = View.GONE
            }

        }

        viewModel.carPoolPreferences.observe(viewLifecycleOwner){
            if (it == null)
                adapter?.isOnlyReadMode(true)
            else
                adapter?.isOnlyReadMode(false)
        }

        binding.recyclerviewDrivers.itemAnimator = ScaleInTopAnimator(OvershootInterpolator(1f))

        viewModel.closeDrivers.observe(viewLifecycleOwner){
            if (it != null && it.isNotEmpty()){
                binding.recyclerviewDrivers.visibility = View.VISIBLE

                adapter?.setList(it)
                binding.recyclerviewDrivers.adapter = adapter

            } else{
                binding.recyclerviewDrivers.visibility = View.GONE

            }
        }

        viewModel.matchedDrivers.observe(viewLifecycleOwner){
            if (it != null && it.isNotEmpty()){
                binding.textviewMatchedDriversTitle.visibility = View.VISIBLE
                binding.imageviewOrangeCircle.visibility = View.VISIBLE
                binding.recyclerviewMatchedDrivers.visibility = View.VISIBLE

                matchedAdapter?.setList(it)
                binding.recyclerviewMatchedDrivers.adapter = matchedAdapter
            } else{

                binding.textviewMatchedDriversTitle.visibility = View.GONE
                binding.recyclerviewMatchedDrivers.visibility = View.GONE
                binding.imageviewOrangeCircle.visibility = View.GONE

            }
        }

    }

    fun showPhoneNumberDialog(itemId: Long) {

        val phoneNumberDialog = CarPoolPhoneNumberDialog(itemId, "rider", object: CarPoolPhoneNumberDialog.PhoneNumberClickListener {
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

    private fun showEndPoolingConfirmation(driverPersonnelId: Long) {

        val dialog = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.setCancelable(false)
        dialog.setMessage(resources.getString(R.string.endpool_carpooling))
        dialog.setPositiveButton(resources.getString(R.string.confirm)) { d, _ ->
            val request = ChooseDriverRequest(driverPersonnelId, false)
            viewModel.setChooseDriver(request)

            d.dismiss()
        }
        dialog.setNegativeButton(resources.getString(R.string.cancel)) { d, _ ->
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
        const val TAG: String = "CarPoolDriverFragment"
        fun newInstance() = CarPoolDriverFragment()
    }


}