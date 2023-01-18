package com.vektortelekom.android.vservice.ui.poolcar.reservation.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.vektor.ktx.utils.ActivityHelper
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.ParkModel
import com.vektortelekom.android.vservice.data.model.PoiModel
import com.vektortelekom.android.vservice.databinding.PoolCarAddReservationFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationActivity
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationViewModel
import com.vektortelekom.android.vservice.ui.poolcar.reservation.dialog.AddReservationDialog
import com.vektortelekom.android.vservice.ui.poolcar.reservation.dialog.AdditionalRidersDialog
import com.vektortelekom.android.vservice.utils.convertForDay
import com.vektortelekom.android.vservice.utils.convertForMonth
import com.vektortelekom.android.vservice.utils.convertForReservationDialog
import com.vektortelekom.android.vservice.utils.convertForTicketFullDate
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject

class PoolCarAddReservationFragment: BaseFragment<PoolCarReservationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarReservationViewModel

    private lateinit var binding: PoolCarAddReservationFragmentBinding

    private var currentSelect: SelectType? = null

    private var additionalRidersDialog: AdditionalRidersDialog? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarAddReservationFragmentBinding>(inflater, R.layout.pool_car_add_reservation_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarAddReservationFragment
            viewModel = this@PoolCarAddReservationFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(BuildConfig.FLAVOR == "tums") {
            binding.textViewTo.visibility = View.VISIBLE
            binding.buttonTo.visibility = View.VISIBLE
            binding.editTextDescription.hint = getString(R.string.tums_pool_car_reservation_suv_info)
            //binding.textViewUsageType.visibility = View.VISIBLE
            //binding.cardViewUsage.visibility = View.VISIBLE
            binding.editTextDutyLocations.hint = getString(R.string.pool_car_duty_locations_hint)
        }

        if(viewModel.isIntercity) {

            viewModel.getPoiList()

            binding.textViewSelectParking.visibility = View.GONE
            binding.buttonSelectParking.visibility = View.GONE
            binding.textViewTo.visibility = View.GONE
            binding.buttonTo.visibility = View.GONE

            binding.textViewVehicleFrom.visibility = View.VISIBLE
            binding.buttonVehicleFrom.visibility = View.VISIBLE
            binding.textViewVehicleTo.visibility = View.VISIBLE
            binding.buttonVehicleTo.visibility = View.VISIBLE
            binding.textViewDutyLocations.visibility = View.VISIBLE
            binding.editTextDutyLocations.visibility = View.VISIBLE

            viewModel.selectedPoiFrom.observe(viewLifecycleOwner) {
                binding.buttonVehicleFrom.text =
                    if (it is PoiModel) it.name else if (it is ParkModel) it.name else ""
                viewModel.availablePriceModels()
            }

            viewModel.selectedPoiTo.observe(viewLifecycleOwner) {
                binding.buttonVehicleTo.text =
                    if (it is PoiModel) it.name else if (it is ParkModel) it.name else ""
                viewModel.availablePriceModels()
            }

        }
        else {
            binding.textViewSelectParking.visibility = View.VISIBLE
            binding.buttonSelectParking.visibility = View.VISIBLE
            binding.textViewTo.visibility = View.VISIBLE
            binding.buttonTo.visibility = View.VISIBLE

            binding.textViewVehicleFrom.visibility = View.GONE
            binding.buttonVehicleFrom.visibility = View.GONE
            binding.textViewVehicleTo.visibility = View.GONE
            binding.buttonVehicleTo.visibility = View.GONE
            binding.textViewDutyLocations.visibility = View.GONE
            binding.editTextDutyLocations.visibility = View.GONE
        }

        viewModel.getStations()
        viewModel.getReservationReasons()

        if(viewModel.selectedStartDate == null) {

            var currentDate = Date()

            var dateTime = DateTime(currentDate.time)

            val minusMinutes = dateTime.minuteOfHour().get()%15
            dateTime = dateTime.plusMinutes(15-minusMinutes)

            currentDate = dateTime.toDate()

            viewModel.selectedStartDate = currentDate
            viewModel.selectedEndDate = Date(currentDate.time + 1000*60*30)
        }



        binding.textViewDateFullDateStart.text = viewModel.selectedStartDate!!.convertForTicketFullDate(requireContext())
        binding.textViewDateDayStart.text = viewModel.selectedStartDate!!.convertForDay()
        binding.textViewDateMonthStart.text = viewModel.selectedStartDate!!.convertForMonth()

        binding.textViewDateFullDateFinish.text = viewModel.selectedEndDate!!.convertForTicketFullDate(requireContext())
        binding.textViewDateDayFinish.text = viewModel.selectedEndDate!!.convertForDay()
        binding.textViewDateMonthFinish.text = viewModel.selectedEndDate!!.convertForMonth()

        binding.buttonSelectParking.setOnClickListener {
            ActivityHelper.hideSoftKeyboard(requireActivity())

            val parks = viewModel.stations.value

            if(parks != null && parks.isNotEmpty()) {

                currentSelect = SelectType.Park

                val displayedValues = Array(parks.size) { "" }
                for(i in parks.indices) {
                    displayedValues[i] = parks[i].name?: ""
                }

                binding.numberPicker.value = 0
                binding.numberPicker.displayedValues = null
                binding.numberPicker.minValue = 0
                binding.numberPicker.maxValue = parks.size-1
                binding.numberPicker.displayedValues = displayedValues
                binding.numberPicker.value = viewModel.selectedParkIndex?:0
                binding.numberPicker.wrapSelectorWheel = true
                binding.textViewSelectTitle.text = getString(R.string.park)

                binding.layoutSelect.visibility = View.VISIBLE

            }
            else {
                viewModel.navigator?.handleError(java.lang.Exception(getString(R.string.warning_select_park_empty)))
            }

        }

        binding.buttonPurposeOfReservation.setOnClickListener {

            ActivityHelper.hideSoftKeyboard(requireActivity())

            val reasons = viewModel.reservationReasons.value



            if(reasons != null && reasons.isNotEmpty()) {

                currentSelect = SelectType.Purpose

                val displayedValues = Array(reasons.size) { "" }
                for(i in reasons.indices) {
                    displayedValues[i] = reasons[i]
                }

                binding.numberPicker.value = 0
                binding.numberPicker.displayedValues = null
                binding.numberPicker.minValue = 0
                binding.numberPicker.maxValue = reasons.size-1
                binding.numberPicker.displayedValues = displayedValues
                binding.numberPicker.value = viewModel.selectedReasonIndex?:0
                binding.numberPicker.wrapSelectorWheel = true
                binding.textViewSelectTitle.text = getString(R.string.purpose_of_reservation)

                binding.layoutSelect.visibility = View.VISIBLE

            }
            else {
                viewModel.navigator?.handleError(Exception(getString(R.string.warning_select_purpose_empty)))
            }

        }

        binding.buttonVehicleType.setOnClickListener {

            ActivityHelper.hideSoftKeyboard(requireActivity())

            val priceModels = viewModel.priceModels.value

            if(priceModels == null) {
                viewModel.navigator?.handleError(Exception(getString(if(viewModel.isIntercity) R.string.warning_pool_car_reservation_intercity_vehicle_null_error else R.string.warning_park_not_selected)))
            }
            else if(priceModels.isEmpty()) {
                viewModel.navigator?.handleError(Exception(getString(if(viewModel.isIntercity) R.string.warning_pool_car_reservation_intercity_vehicle_empty_error else R.string.warning_park_has_not_car)))
            }
            else
            {

                currentSelect = SelectType.VehicleType

                val displayedValues = Array(priceModels.size) { "" }
                for(i in priceModels.indices) {
                    displayedValues[i] = priceModels[i].description?:""
                }

                binding.numberPicker.value = 0
                binding.numberPicker.displayedValues = null
                binding.numberPicker.minValue = 0
                binding.numberPicker.maxValue = priceModels.size-1
                binding.numberPicker.displayedValues = displayedValues
                binding.numberPicker.value = viewModel.selectedPriceModelIndex?:0
                binding.numberPicker.wrapSelectorWheel = true
                binding.textViewSelectTitle.text = getString(R.string.vehicle_type)

                binding.layoutSelect.visibility = View.VISIBLE

            }
        }

        binding.buttonSelectSelect.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
            when(currentSelect) {
                SelectType.Park -> {
                    viewModel.selectedPark = viewModel.stations.value?.get(binding.numberPicker.value)
                    viewModel.selectedParkIndex = binding.numberPicker.value
                    binding.buttonSelectParking.text = viewModel.selectedPark?.name


                    clearSelectedPriceModel()

                    viewModel.selectedPark?.let {
                        viewModel.availablePriceModels()
                        //viewModel.getStationVehicles(it.id)
                    }

                }
                SelectType.Purpose -> {
                    viewModel.selectedReason = viewModel.reservationReasons.value?.get(binding.numberPicker.value)
                    viewModel.selectedReasonIndex = binding.numberPicker.value
                    binding.buttonPurposeOfReservation.text = viewModel.selectedReason
                }
                SelectType.VehicleType -> {
                    viewModel.selectedPriceModel = viewModel.priceModels.value?.get(binding.numberPicker.value)
                    viewModel.selectedPriceModelIndex = binding.numberPicker.value
                    binding.buttonVehicleType.text = viewModel.selectedPriceModel?.description
                }
                else -> {

                }
            }
        }

        binding.cardViewReservationStart.setOnClickListener {
            ActivityHelper.hideSoftKeyboard(requireActivity())

            SingleDateAndTimePickerDialog.Builder(context)
                    .defaultDate(viewModel.selectedStartDate?:Date())
                    .bottomSheet()
                    .minutesStep(15)
                    .curved()
                    .todayText(getString(R.string.today))
                    .title(getString(R.string.demand_date))
                    .minDateRange(Date())
                    .listener { selectedDate ->
                        viewModel.selectedStartDate = selectedDate

                        binding.textViewDateFullDateStart.text = selectedDate.convertForTicketFullDate(requireContext())
                        binding.textViewDateDayStart.text = selectedDate.convertForDay()
                        binding.textViewDateMonthStart.text = selectedDate.convertForMonth()


                        if((viewModel.selectedEndDate?:Date()).time <= selectedDate.time) {
                            val endDate = Date(selectedDate.time + 1000*60*60)
                            viewModel.selectedEndDate = endDate

                            binding.textViewDateFullDateFinish.text = endDate.convertForTicketFullDate(requireContext())
                            binding.textViewDateDayFinish.text = endDate.convertForDay()
                            binding.textViewDateMonthFinish.text = endDate.convertForMonth()
                        }

                        clearSelectedPriceModel()

                        viewModel.availablePriceModels()


                    }
                    .customLocale(Locale("tr", "TR"))
                    .titleTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
                    .mainColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
                    .display()
        }

        binding.cardViewReservationFinish.setOnClickListener {
            ActivityHelper.hideSoftKeyboard(requireActivity())

            SingleDateAndTimePickerDialog.Builder(context)
                    .defaultDate(viewModel.selectedEndDate?:Date(Date().time + 1000*60*30))
                    .bottomSheet()
                    .minutesStep(15)
                    .curved()
                    .todayText(getString(R.string.today))
                    .title(getString(R.string.demand_date))
                    .minDateRange(Date((viewModel.selectedStartDate?:Date()).time + 1000*60*30) )
                    .listener { selectedDate ->
                        viewModel.selectedEndDate = selectedDate

                        binding.textViewDateFullDateFinish.text = selectedDate.convertForTicketFullDate(requireContext())
                        binding.textViewDateDayFinish.text = selectedDate.convertForDay()
                        binding.textViewDateMonthFinish.text = selectedDate.convertForMonth()

                        clearSelectedPriceModel()

                        viewModel.availablePriceModels()

                    }
                    .customLocale(Locale("tr", "TR"))
                    .titleTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
                    .mainColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
                    .display()
        }

        binding.buttonSelectCancel.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
        }

        binding.buttonMakeReservation.setOnClickListener {

            if(viewModel.isIntercity.not() && viewModel.selectedPark == null) {
                viewModel.navigator?.handleError(Exception(getString(R.string.warning_reservation_select_park)))
            }
            else if(viewModel.isIntercity.not() && viewModel.reservationToLocation.value == null && BuildConfig.FLAVOR == "tums") {
                viewModel.navigator?.handleError(Exception(getString(R.string.warning_reservation_select_to)))
            }
            else if(viewModel.isIntercity && viewModel.selectedPoiFrom.value == null) {
                viewModel.navigator?.handleError(Exception(getString(R.string.warning_reservation_select_from)))
            }
            else if(viewModel.isIntercity && viewModel.selectedPoiTo.value == null) {
                viewModel.navigator?.handleError(Exception(getString(R.string.warning_reservation_select_to)))
            }
            else if(viewModel.isIntercity && viewModel.dutyLocations.value.isNullOrBlank()) {
                viewModel.navigator?.handleError(Exception(getString(R.string.duty_locations_empty)))
            }
            else if(viewModel.selectedReason == null) {
                viewModel.navigator?.handleError(Exception(getString(R.string.warning_reservation_select_purpose)))
            }
            else if(viewModel.selectedStartDate == null) {
                viewModel.navigator?.handleError(Exception(getString(R.string.warning_reservation_select_start_date)))
            }
            else if(viewModel.selectedEndDate == null){
                viewModel.navigator?.handleError(Exception(getString(R.string.warning_reservation_select_finish_date)))
            }
            else if(viewModel.description.value.isNullOrEmpty()) {
                viewModel.navigator?.handleError(Exception(getString(R.string.description_empty)))
            }
            else if((viewModel.selectedEndDate?:Date()).time - (viewModel.selectedStartDate?:Date()).time < 1000*60*30) {
                viewModel.navigator?.handleError(Exception(getString(R.string.pool_car_reservation_start_end_diff)))
            }
            else if(viewModel.selectedPriceModel == null) {
                viewModel.navigator?.handleError(Exception(view.context.getString(R.string.vehicle_price_model_empty)))
            }
            else {
                val dialog = AddReservationDialog(
                        requireContext(),
                        viewModel.selectedPark?.name?:"",
                        viewModel.reservationAddressTextTo.value?:"",
                        viewModel.selectedReason?:"",
                viewModel.selectedStartDate.convertForReservationDialog(),
                viewModel.selectedEndDate.convertForReservationDialog(),
                viewModel.description.value?:"",
                object : AddReservationDialog.AddReservationListener {
                    override fun addReservation() {
                        viewModel.submitReservation(null)
                    }

                })
                dialog.show()
            }

        }

        /*binding.textViewLocal.setOnClickListener {

            viewModel.travelDestinationType = TravelDestinationType.LOCAL

            binding.textViewLocal.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.textViewLocal.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.textViewIntercity.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.textViewIntercity.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))

            clearSelectedPriceModel()

            viewModel.availablePriceModels()
        }*/

        /*binding.textViewIntercity.setOnClickListener {

            viewModel.travelDestinationType = TravelDestinationType.INTERCITY

            binding.textViewIntercity.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.textViewIntercity.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.textViewLocal.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.textViewLocal.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))

            clearSelectedPriceModel()

            viewModel.availablePriceModels()
        }*/

        viewModel.reservationAddressTextTo.observe(viewLifecycleOwner) {
            binding.buttonTo.text = it
        }

        viewModel.reservationAddResponse.observe(viewLifecycleOwner) {
            FlexigoInfoDialog.Builder(requireContext())
                .setIconVisibility(false)
                .setCancelable(true)
                .setTitle(getString(R.string.reservation_success_title))
                .setText1(getString(R.string.reservation_success_info_text))
                .setOkButton(getString(R.string.show_reservation_list)) { dialog ->
                    dialog.dismiss()

                    if (requireActivity() is PoolCarReservationActivity) {
                        (requireActivity() as PoolCarReservationActivity).showPoolCarReservationsFragment(
                            null
                        )
                    }

                }
                .create()
                .show()
        }

        binding.buttonAdditionalRiders.setOnClickListener {

            additionalRidersDialog = AdditionalRidersDialog(requireContext(), viewModel.personList, { searchText ->
                viewModel.searchPersonWithRegistrationNumber(searchText)

            },{
                additionalRidersDialog = null
                var buttonText = ""
                for(person in viewModel.personList) {
                    buttonText = buttonText.plus(person.fullName).plus(", ")
                }
                if(buttonText.isNotEmpty()) {
                    buttonText = buttonText.substring(0, buttonText.length-2)
                }
                binding.buttonAdditionalRiders.text = buttonText
            })
            additionalRidersDialog?.show()
        }

        viewModel.searchPersonResult.observe(viewLifecycleOwner) { person ->
            if(additionalRidersDialog?.isShowing == true) {
                additionalRidersDialog?.searchPersonCallback(person)
            }
        }

        binding.buttonVehicleFrom.setOnClickListener {

            ActivityHelper.hideSoftKeyboard(requireActivity())

            viewModel.isSelectVehicleFromOrTo = true
            viewModel.navigator?.showSelectPoiFragment()

        }

        binding.buttonVehicleTo.setOnClickListener {

            ActivityHelper.hideSoftKeyboard(requireActivity())

            viewModel.isSelectVehicleFromOrTo = false
            viewModel.navigator?.showSelectPoiFragment()

        }

        binding.editTextDescription.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.editTextDescription.setRawInputType(InputType.TYPE_CLASS_TEXT)

        binding.editTextDutyLocations.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.editTextDutyLocations.setRawInputType(InputType.TYPE_CLASS_TEXT)

        binding.imageViewDescription.setOnClickListener {
            FlexigoInfoDialog.Builder(requireContext())
                    .setIconVisibility(false)
                    .setCancelable(true)
                    .setTitle("")
                    .setText1(getString(R.string.pool_car_reservation_description_dialog_text))
                    .setOkButton(getString(R.string.Generic_Ok)) {
                        it.dismiss()
                    }
                    .create()
                    .show()
        }

    }

    private fun clearSelectedPriceModel() {
        binding.buttonVehicleType.text = getString(R.string.select)
        viewModel.selectedPriceModel = null
        viewModel.selectedPriceModelIndex = null
    }

    enum class SelectType {
        Park,
        Purpose,
        VehicleType
    }

    override fun getViewModel(): PoolCarReservationViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarReservationViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarAddReservationFragment"

        fun newInstance() = PoolCarAddReservationFragment()

    }

}