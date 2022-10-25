package com.vektortelekom.android.vservice.ui.carpool.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CompoundButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.CarPoolPreferencesRequest
import com.vektortelekom.android.vservice.databinding.CarpoolFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.ui.carpool.adapter.ViewPagerAdapter
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import javax.inject.Inject

class CarPoolFragment : BaseFragment<CarPoolViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CarPoolViewModel

    lateinit var binding: CarpoolFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CarpoolFragmentBinding>(inflater, R.layout.carpool_fragment, container, false).apply {
            lifecycleOwner = this@CarPoolFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(CarPoolDriverFragment(), "Drivers")
        adapter.addFragment(CarPoolRiderFragment(), "Riders")

        binding.viewPager.adapter = adapter
        binding.tablayout.setupWithViewPager(binding.viewPager)

        viewModel.carPoolPreferences.observe(viewLifecycleOwner) {

            if (it == null) {
                binding.switchDriver.visibility = View.GONE
                binding.buttonOptIn.visibility = View.VISIBLE
                binding.layoutLikeMenu.visibility = View.GONE

                showNotUsingCarpoolDialog()

            } else {

                binding.buttonOptIn.visibility = View.GONE
                binding.switchDriver.visibility = View.VISIBLE

                binding.textviewArrivalValue.text = it.arrivalHour.convertHourMinutes()?: ""
                binding.textviewDepartureValue.text = it.departureHour.convertHourMinutes()?: ""

                if (AppDataManager.instance.showCarpoolInfoDialog == false)
                    showDialog()
            }
        }

        binding.buttonOptIn.setOnClickListener {
            showNotUsingCarpoolDialog()
        }

        viewModel.isDriver.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.switchDriver.isChecked = it
                setVisibilityLikeMenu(binding.viewPager.currentItem, it)
            }
        }

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                viewModel.viewPagerCurrentItem.value = binding.viewPager.currentItem

                viewModel.isDriver.value?.let {
                    setVisibilityLikeMenu(
                        binding.viewPager.currentItem,
                        it
                    )
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        viewModel.viewPagerCurrentItem.observe(viewLifecycleOwner) {
            if (it != null)
                if (it == 1 && viewModel.isDriver.value == false)
                    showInvitationDialog()
        }

        binding.switchDriver.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (binding.viewPager.currentItem == 0){
                    buttonView.isChecked = !isChecked
                    showSwitchOfWarningDialog(buttonView, isChecked)
                }
            }

        }

        binding.imageviewQuestionMark.setOnClickListener {
            showDialog()
        }

        binding.imageviewArrivalEdit.setOnClickListener {
            showTimePicker("arrival")
        }

        binding.imageviewDepartureEdit.setOnClickListener {
            showTimePicker("departure")
        }

        viewModel.arrivalHour.observe(viewLifecycleOwner){
            if (it != null) {
                binding.textviewArrivalValue.text = it.convertHourMinutes()
                val request = CarPoolPreferencesRequest(null,null,it,null)
                viewModel.updateCarPoolPreferences(request)
            }
        }

        viewModel.departureHour.observe(viewLifecycleOwner){
            if (it != null) {
                binding.textviewDepartureValue.text = it.convertHourMinutes()
                val request = CarPoolPreferencesRequest(null,null, null, it)
                viewModel.updateCarPoolPreferences(request)
            }
        }

    }

    private fun showTimePicker(hours: String){

        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(13)
                .setMinute(15)
                .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                .build()
        picker.show(childFragmentManager, "tag")

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute

            if (hours == "arrival")
                viewModel.arrivalHour.value = hour.toString().plus(minute).toInt()
            else
                viewModel.departureHour.value = hour.toString().plus(minute).toInt()
        }

    }

    private fun showNotUsingCarpoolDialog() {

        val dialog = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.setCancelable(false)
        dialog.setTitle(resources.getString(R.string.would_you_like_carpooling))
        dialog.setMessage(resources.getString(R.string.would_you_like_carpooling_text))
        dialog.setPositiveButton(resources.getString(R.string.opt_in_driver)) { d, _ ->
            viewModel.isDriver.value = true
            viewModel.isRider.value = false

            val request = CarPoolPreferencesRequest(viewModel.isDriver.value!!, viewModel.isRider.value!!,null,null)
            viewModel.updateCarPoolPreferences(request)

            d.dismiss()
        }
        dialog.setNegativeButton(resources.getString(R.string.opt_in_rider)) { d, _ ->
            viewModel.isDriver.value = true
            viewModel.isRider.value = false

            val request = CarPoolPreferencesRequest(viewModel.isDriver.value!!, viewModel.isRider.value!!,null,null)
            viewModel.updateCarPoolPreferences(request)

            d.dismiss()
        }
        dialog.setNeutralButton(resources.getString(R.string.just_view)) { d, _ ->
            d.dismiss()
        }

        dialog.show()

    }

    private fun setVisibilityLikeMenu(currentPage: Int, isDriver: Boolean) {
        if (currentPage == 0 && isDriver)
            binding.layoutLikeMenu.visibility = View.GONE
        else if (currentPage == 0 && !isDriver)
            binding.layoutLikeMenu.visibility = View.VISIBLE
        else if (currentPage == 1 && !isDriver)
            binding.layoutLikeMenu.visibility = View.GONE
    }

    private fun showSwitchOfWarningDialog(button: CompoundButton, isChecked: Boolean) {

        val warningText = if (isChecked)
            resources.getString(R.string.change_my_status_text2)
        else
            resources.getString(R.string.change_my_status_text)

        val dialog = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.setCancelable(false)
        dialog.setMessage(warningText)
        dialog.setPositiveButton(resources.getString(R.string.change_my_status)) { d, _ ->
            viewModel.isDriver.value = isChecked
            viewModel.isRider.value = !isChecked

            button.isChecked = isChecked

            val request = CarPoolPreferencesRequest(viewModel.isDriver.value!!, viewModel.isRider.value!!,null,null)
            viewModel.updateCarPoolPreferences(request)

            d.dismiss()
        }
        dialog.setNegativeButton(resources.getString(R.string.cancel)) { d, _ ->
            d.dismiss()
        }

        dialog.show()

    }

    private fun showInvitationDialog() {

        val dialog = Dialog(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.attributes?.gravity = Gravity.BOTTOM
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_warning_dialog)

        val buttonOk = dialog.findViewById(R.id.button_ok) as Button
        val buttonNo = dialog.findViewById(R.id.button_no) as Button

        buttonOk.setOnClickListener {
            binding.switchDriver.isChecked = true
            viewModel.isDriver.value = true
            viewModel.isRider.value = false

            val request = CarPoolPreferencesRequest(viewModel.isDriver.value!!, viewModel.isRider.value!!,null,null)
            viewModel.updateCarPoolPreferences(request)
            dialog.dismiss()
        }

        buttonNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDialog() {
        AppDataManager.instance.showCarpoolInfoDialog = true

        val dialog = Dialog(requireContext(), R.style.MaterialAlertDialogRounded)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.carpool_info_dialog)

        val buttonOk = dialog.findViewById(R.id.button_ok) as Button
        buttonOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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