package com.vektortelekom.android.vservice.ui.flexiride.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.format.DateFormat
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.ActivityHelper
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.CountryCodeResponseListModel
import com.vektortelekom.android.vservice.data.model.FlexirideOffer
import com.vektortelekom.android.vservice.databinding.FlexirideFromFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.base.CustomCountryListAdapter
import com.vektortelekom.android.vservice.ui.dialog.CustomTimePickerDialog
import com.vektortelekom.android.vservice.ui.flexiride.FlexirideViewModel
import com.vektortelekom.android.vservice.ui.flexiride.adapter.FlexirideOfferListAdapter
import com.vektortelekom.android.vservice.ui.poolcar.reservation.dialog.AdditionalRidersDialog
import com.vektortelekom.android.vservice.ui.poolcar.reservation.fragment.PoolCarAddReservationFragment
import com.vektortelekom.android.vservice.utils.*
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject


class FlexiRideFromFragment: BaseFragment<FlexirideViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: FlexirideViewModel

    lateinit var binding: FlexirideFromFragmentBinding

    private lateinit var googleMap: GoogleMap

    private lateinit var locationClient: FusedLocationClient

    private var fromPinIcon: BitmapDescriptor? = null
    private var toPinIcon: BitmapDescriptor? = null

    private var pickUpPinIcon: BitmapDescriptor? = null
    private var dropOffPinIcon: BitmapDescriptor? = null

    private var fromMarker: Marker? = null
    private var toMarker: Marker? = null

    private var offerFromMarker: Marker? = null
    private var offerToMarker: Marker? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var childSeatCount = 0

    private var offerList: MutableList<FlexirideOffer>? = null

    private var isFromAndToSelected = false

    private var additionalRidersDialog: AdditionalRidersDialog? = null

    private var currentSelect: PoolCarAddReservationFragment.SelectType? = null

    private var isBeforeFirstPlusClicked = false

    var adapter : CustomCountryListAdapter? = null

    private var countryCode : String? = null
    var characterCount : Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<FlexirideFromFragmentBinding>(inflater, R.layout.flexiride_from_fragment, container, false).apply {
            lifecycleOwner = this@FlexiRideFromFragment
            viewModel = this@FlexiRideFromFragment.viewModel
        }

        return binding.root
    }


    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)

        val phoneUtil = PhoneNumberUtil.getInstance()
        setupBackPressListener()

        viewModel.getFlexirideReasons()

        binding.mapView.getMapAsync { googleMap ->

            this.googleMap = googleMap

            fromPinIcon = BitmapDescriptorFactory.fromBitmap(createStoreMarker(R.layout.flexiride_map_marker_from))
            toPinIcon = BitmapDescriptorFactory.fromBitmap(createStoreMarker(R.layout.flexiride_map_marker_to))
            pickUpPinIcon = BitmapDescriptorFactory.fromBitmap(createStoreMarker(R.layout.flexiride_map_marker_pick_up))
            dropOffPinIcon = BitmapDescriptorFactory.fromBitmap(createStoreMarker(R.layout.flexiride_map_marker_drop_off))


            when(viewModel.type) {
                FlexirideViewModel.FlexirideCreateType.NORMAL -> {
                    binding.textViewAdditionalRiders.visibility = View.GONE
                    binding.buttonAdditionalRiders.visibility = View.GONE

                    binding.editTextNameSurname.visibility = View.GONE
                    binding.layoutPhone.visibility = View.GONE

                }
                FlexirideViewModel.FlexirideCreateType.GUEST -> {
                    binding.textViewAdditionalRiders.visibility = View.GONE
                    binding.buttonAdditionalRiders.visibility = View.GONE

                    binding.editTextNameSurname.visibility = View.VISIBLE
                    binding.layoutPhone.visibility = View.VISIBLE
                }

            }


            viewModel.toLocation.value?.let { location ->
                viewModel.addressTextTo?.let { addressText ->
                    binding.textViewTo.text = addressText
                }
                toMarker?.remove()
                toMarker = googleMap.addMarker(MarkerOptions().position(location).icon(toPinIcon))
                toMarker?.isDraggable = true
                toMarker?.title = "toMarker"

                binding.layoutTo.visibility = View.VISIBLE
            }

            continueAfterMapInitialized()

            googleMap.setOnMarkerClickListener { marker ->

                marker.hideInfoWindow()
                true
            }

            googleMap.setOnMarkerDragListener(object : OnMarkerDragListener {
                override fun onMarkerDrag(p0: Marker) {
                }

                override fun onMarkerDragEnd(marker: Marker) {

                    val geoCoder = Geocoder(requireContext(), Locale(getString(R.string.generic_language)))
                    try {
                        val addresses = geoCoder.getFromLocation(marker.position.latitude, marker.position.longitude, 1)

                        if (addresses.size > 0) {
                            val address = addresses[0]

                            if (marker.title.equals("toMarker")){
                                binding.textViewTo.text = address.getAddressLine(0)
                            } else{
                                binding.textViewFrom.text = address.getAddressLine(0)
                            }


                        }
                    } catch (e: Exception) {
                    }
                }

                override fun onMarkerDragStart(p0: Marker) {
                }
            })

            googleMap.setOnCameraIdleListener {

                if(isFromAndToSelected) {
                    return@setOnCameraIdleListener
                }

                if(viewModel.isFrom) {
                    viewModel.fromLocation.value = googleMap.cameraPosition.target
                }
                else {
                    viewModel.toLocation.value = googleMap.cameraPosition.target
                }

                val geoCoder = Geocoder(requireContext(), Locale(getString(R.string.generic_language)))

                try{
                    val addresses = geoCoder.getFromLocation(googleMap.cameraPosition.target.latitude, googleMap.cameraPosition.target.longitude, 1)

                    if(addresses.size > 0) {
                        val address = addresses[0]
                        if(viewModel.isFrom) {
                            viewModel.addressTextFrom = address.getAddressLine(0)
                            binding.textViewFrom.text = address.getAddressLine(0)
                        }
                        else {
                            viewModel.addressTextTo = address.getAddressLine(0)
                            binding.textViewTo.text = address.getAddressLine(0)
                        }

                    }
                }
                catch (e: Exception) {
                    if(viewModel.isFrom) {
                        viewModel.addressTextFrom = ""
                        binding.textViewFrom.text = ""
                    }
                    else {
                        viewModel.addressTextTo = ""
                        binding.textViewTo.text = ""
                    }
                }

            }

            viewModel.fromLocation.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (isFromAndToSelected && viewModel.isFrom){
                    fromMarker?.remove()
                    fromMarker = googleMap.addMarker(MarkerOptions().position(it).icon(fromPinIcon))
                    fromMarker?.isDraggable = true
                    fromMarker?.title = "fromMarker"

                    val geoCoder = Geocoder(requireContext(), Locale(getString(R.string.generic_language)))

                    try {
                        val addresses = geoCoder.getFromLocation(it.latitude, it.longitude, 1)

                        if (addresses.size > 0) {
                            val address = addresses[0]
                            binding.textViewFrom.text = address.getAddressLine(0)

                        }
                    } catch (e: Exception) {
                        binding.textViewFrom.text = ""
                    }
                }

                if(googleMap.cameraPosition.target.latitude == it.latitude && googleMap.cameraPosition.target.longitude == it.longitude) {
                    return@Observer
                }
                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 19f)
                googleMap.moveCamera(cu)


            })

            viewModel.toLocation.observe(viewLifecycleOwner) { toLatLng ->
                if (isFromAndToSelected && !viewModel.isFrom){
                    toMarker?.remove()
                    toMarker = googleMap.addMarker(MarkerOptions().position(toLatLng).icon(toPinIcon))
                    toMarker?.isDraggable = true
                    toMarker?.title = "toMarker"
                }

                val geoCoder = Geocoder(requireContext(), Locale(getString(R.string.generic_language)))

                try {
                    val addresses = geoCoder.getFromLocation(toLatLng.latitude, toLatLng.longitude, 1)

                    if (addresses.size > 0) {
                        val address = addresses[0]
                        binding.textViewTo.text = address.getAddressLine(0)

                    }
                } catch (e: Exception) {
                    binding.textViewTo.text = ""
                }

                if (viewModel.shouldCameraNavigateTo) {
                    continueAfterFromSelected()
                    val cu = CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            toLatLng.latitude,
                            toLatLng.longitude
                        ), 19f
                    )
                    googleMap.moveCamera(cu)
                    viewModel.shouldCameraNavigateTo = false
                }

            }

        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    ActivityHelper.hideSoftKeyboard(requireActivity())
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })
        viewModel.passengerCountString.value = "1"

        binding.cardViewPassengerPlus.setOnClickListener {

            if(isBeforeFirstPlusClicked.not() && viewModel.type == FlexirideViewModel.FlexirideCreateType.NORMAL) {
                binding.textViewAdditionalRiders.visibility = View.VISIBLE
                binding.buttonAdditionalRiders.visibility = View.VISIBLE
            }

            if(viewModel.passengerCount == 1) {
                binding.cardViewPassengerMinus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.steel))
            }

            viewModel.passengerCount++
            viewModel.passengerCountString.value = viewModel.passengerCount.toString()

        }

        binding.cardViewPassengerMinus.setOnClickListener {
            if(viewModel.passengerCount < 2) {
                return@setOnClickListener
            }

            viewModel.passengerCount--
            viewModel.passengerCountString.value = viewModel.passengerCount.toString()

            if(viewModel.passengerCount < 2) {
                binding.cardViewPassengerMinus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.paleGrey2))
            }
        }

        binding.cardViewChildSeatPlus.setOnClickListener {
            if(childSeatCount == 0) {
                binding.cardViewChildSeatMinus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.steel))
            }

            childSeatCount++
            viewModel.childSeatCountString.value = childSeatCount.toString()
        }

        binding.cardViewChildSeatMinus.setOnClickListener {
            if(childSeatCount < 1) {
                return@setOnClickListener
            }

            childSeatCount--
            viewModel.childSeatCountString.value = childSeatCount.toString()

            if(childSeatCount < 1) {
                binding.cardViewChildSeatMinus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.paleGrey2))
            }
        }

        binding.textViewNow.setOnClickListener {

            val selectedDate = Date()
            viewModel.selectedDate = selectedDate

            binding.textViewDateFullDate.text = selectedDate.convertForShuttleDay()
            binding.textViewDateDay.text = selectedDate.convertForDay()
            binding.textViewDateMonth.text = selectedDate.convertForMonth()

            binding.textViewNow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.textViewNow.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.textViewLater.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.textViewLater.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.layoutDate.visibility = View.GONE
        }

        binding.textViewLater.setOnClickListener {
            binding.textViewLater.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.textViewLater.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.textViewNow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.textViewNow.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.layoutDate.visibility = View.VISIBLE
        }

        var currentDate: Date

        if(viewModel.selectedDate == null) {
            currentDate = Date()
        }
        else {
            currentDate = viewModel.selectedDate?:Date()
            binding.textViewLater.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.textViewLater.setTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.textViewNow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.steel))
            binding.textViewNow.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            binding.layoutDate.visibility = View.VISIBLE
        }

        var dateTime = DateTime(currentDate.time)

        val minusMinutes = dateTime.minuteOfHour().get()%15
        dateTime = dateTime.plusMinutes(15-minusMinutes)

        currentDate = dateTime.toDate()

        viewModel.selectedDate = currentDate

        val defaultHour = if (DateFormat.is24HourFormat(context))
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        else
            Calendar.getInstance().get(Calendar.HOUR)

        val defaultMin = Calendar.getInstance().get(Calendar.MINUTE)

        viewModel.dateTime.value = setDateTime(defaultHour, defaultMin)

        binding.textViewDateTime.text = setDateTime(defaultHour, defaultMin)
        binding.textViewDateFullDate.text = currentDate.convertForShuttleDay()
        binding.textViewDateDay.text = currentDate.convertForDay()
        binding.textViewDateMonth.text = currentDate.convertForMonth()

        binding.textViewDateFullDate.setOnClickListener {
            showDatePicker()
        }

        viewModel.createFlexirideResponse.observe(viewLifecycleOwner) {

            val builder = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded).create()

            val viewDialog = layoutInflater.inflate(R.layout.message_dialog, null)

            val button = viewDialog.findViewById<Button>(R.id.other_button)
            val closeButton = viewDialog.findViewById<Button>(R.id.negative_button)
            val icon = viewDialog.findViewById<AppCompatImageView>(R.id.imageview_icon)
            val title = viewDialog.findViewById<TextView>(R.id.textview_subtitle)
            val subTitle = viewDialog.findViewById<TextView>(R.id.textview_title)

            title.text = getString(R.string.show_request_screen)
            subTitle.text = getString(R.string.request_has_been_successfull)
            button.text = getString(R.string.view_my_request)

            closeButton.visibility = View.VISIBLE

            icon.setBackgroundResource(R.drawable.ic_check)

            builder.setView(viewDialog)

            builder.setCanceledOnTouchOutside(false)
            builder.show()

            button.setOnClickListener {
                viewModel.navigator?.showFlexirideListFragment(null)
                builder.dismiss()
            }

            closeButton.setOnClickListener {
                builder.dismiss()
                activity?.finish()
            }

        }

        binding.buttonGetOffer.setOnClickListener {
            binding.layoutOfferForm.visibility = View.GONE
            binding.layoutOffers.visibility = View.VISIBLE

            offerList = mutableListOf()
            offerList?.add(FlexirideOffer("10:00", "11:30", "test1", "test2", 8, 13.65, true, LatLng(41.070905, 29.006448), LatLng(41.048901, 29.007089)))
            offerList?.add(FlexirideOffer("10:10", "11:45", "test1", "test3", 6, 12.40, true, LatLng(41.024616, 29.022606), LatLng(40.994855, 29.025783)))
            offerList?.add(FlexirideOffer("10:00", "11:30", "test1", "test2", 7, 13.20, true, LatLng(41.010482, 28.965696), LatLng(40.989241, 28.897815)))

            binding.recyclerViewOffers.adapter = FlexirideOfferListAdapter(offerList?: mutableListOf(), object: FlexirideOfferListAdapter.OfferSelectListener {
                override fun offerSelected(offer: FlexirideOffer) {

                    binding.layoutOffers.visibility = View.GONE
                    binding.layoutFlexirideRoute.visibility = View.VISIBLE

                }

            })

            offerFromMarker?.remove()
            offerToMarker?.remove()

            if((offerList?.size ?: 0) > 0) {
                val offer = offerList!![0]
                offerFromMarker = googleMap.addMarker(MarkerOptions().position(offer.startLatLng).icon(pickUpPinIcon))
                offerToMarker = googleMap.addMarker(MarkerOptions().position(offer.finishLatLng).icon(dropOffPinIcon))

                val minLat = if(offer.startLatLng.latitude < offer.finishLatLng.latitude) offer.startLatLng.latitude else offer.finishLatLng.latitude
                val minLng = if(offer.startLatLng.longitude < offer.finishLatLng.longitude) offer.startLatLng.longitude else offer.finishLatLng.longitude
                val maxLat = if(offer.startLatLng.latitude > offer.finishLatLng.latitude) offer.startLatLng.latitude else offer.finishLatLng.latitude
                val maxLng = if(offer.startLatLng.longitude > offer.finishLatLng.longitude) offer.startLatLng.longitude else offer.finishLatLng.longitude

                val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 100)
                googleMap.moveCamera(cu)
            }

        }

        binding.recyclerViewOffers.addOnItemChangedListener { _, i ->
            offerList?.let { offerList ->
                val offer = offerList[i]
                offerFromMarker?.remove()
                offerToMarker?.remove()

                offerFromMarker = googleMap.addMarker(MarkerOptions().position(offer.startLatLng).icon(pickUpPinIcon))
                offerToMarker = googleMap.addMarker(MarkerOptions().position(offer.finishLatLng).icon(dropOffPinIcon))

                val minLat = if(offer.startLatLng.latitude < offer.finishLatLng.latitude) offer.startLatLng.latitude else offer.finishLatLng.latitude
                val minLng = if(offer.startLatLng.longitude < offer.finishLatLng.longitude) offer.startLatLng.longitude else offer.finishLatLng.longitude
                val maxLat = if(offer.startLatLng.latitude > offer.finishLatLng.latitude) offer.startLatLng.latitude else offer.finishLatLng.latitude
                val maxLng = if(offer.startLatLng.longitude > offer.finishLatLng.longitude) offer.startLatLng.longitude else offer.finishLatLng.longitude

                val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 100)
                googleMap.moveCamera(cu)
            }
        }

        binding.buttonAdditionalRiders.setOnClickListener {

            additionalRidersDialog = AdditionalRidersDialog(requireContext(), viewModel.personList, { searchText ->
                viewModel.searchPersonWithRegistrationNumber(searchText)

            },{

                viewModel.passengerCount = viewModel.personList.size + 1

                viewModel.passengerCountString.value = viewModel.passengerCount.toString()

                if(viewModel.passengerCount > 1) {
                    binding.cardViewPassengerMinus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.steel))
                }
                else {
                    binding.cardViewPassengerMinus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.paleGrey2))
                }

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
                if (person != null) {
                    additionalRidersDialog?.searchPersonCallback(person)
                }
            }
        }

        binding.buttonPurposeOfFlexiride.setOnClickListener {

            ActivityHelper.hideSoftKeyboard(requireActivity())

            val reasons = viewModel.flexirideReasons.value

            if(reasons != null && reasons.isNotEmpty()) {

                currentSelect = PoolCarAddReservationFragment.SelectType.Purpose

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
                binding.textViewSelectTitle.text = getString(R.string.purpose_of_flexiride)

                binding.layoutSelect.visibility = View.VISIBLE

            }
            else {
                viewModel.navigator?.handleError(Exception(getString(R.string.warning_select_purpose_empty)))
            }
        }

        binding.buttonSelectSelect.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
            when(currentSelect) {
                PoolCarAddReservationFragment.SelectType.Purpose -> {
                    viewModel.selectedReason = viewModel.flexirideReasons.value?.get(binding.numberPicker.value)
                    viewModel.selectedReasonIndex = binding.numberPicker.value
                    binding.buttonPurposeOfFlexiride.text = viewModel.selectedReason
                }
                else -> {

                }
            }
        }

        binding.buttonSelectCancel.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
        }

        binding.textViewDateTime.setOnClickListener {
            showTimePicker()
        }

        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isDisabledRider = isChecked
        }

        setCountryCode()

        viewModel.countryCode.observe(viewLifecycleOwner){
            adapter = CustomCountryListAdapter(requireContext(), R.layout.textview, it)
            binding.autoCompleteTextView.setAdapter(adapter)

            viewModel.areaCode.value = it.first().areaCode
            binding.autoCompleteTextView.setText("+ ".plus(it.first().areaCode))
            binding.autoCompleteTextView.inputType = InputType.TYPE_NULL
        }

        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val item = "+".plus(adapter?.getItem(position)?.areaCode)
            binding.autoCompleteTextView.setText(item)

            viewModel.areaCode.value = adapter?.getItem(position)?.areaCode
            viewModel.phoneNumber.value = ""
        }

        viewModel.areaCode.observe(viewLifecycleOwner){
            if (it != null){
                try {
                    countryCode = phoneUtil.getRegionCodesForCountryCode(it.toInt()).first()

                    val nationalNumber = phoneUtil.getInvalidExampleNumber(countryCode.toString()).nationalNumber
                    characterCount = nationalNumber.toString().length + 1

                    binding.editTextPhoneNumber.hint = nationalNumber.toString()
                    binding.editTextPhoneNumber.filters = arrayOf<InputFilter>(
                        InputFilter.LengthFilter(characterCount!!)
                    )

                } catch (e: NumberParseException) {
                    System.err.println("NumberParseException was thrown: $e")
                }
            }
        }

        binding.editTextDescription.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.editTextDescription.setRawInputType(InputType.TYPE_CLASS_TEXT)

    }

    private fun setCountryCode(){
        val list : MutableList<CountryCodeResponseListModel> = ArrayList()

        val codeOfUS = CountryCodeResponseListModel(shortCode = "US", "", "1")
        val codeOfTurkey = CountryCodeResponseListModel(shortCode = "TR", "", "90")

        list.add(codeOfTurkey)
        list.add(codeOfUS)

        viewModel.countryCode.value = list

    }

    private fun showDatePicker(){
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireActivity(), { view, _, monthOfYear, dayOfMonth ->

            viewModel.selectedDate = view.getDate()

            binding.textViewDateDay.text = dayOfMonth.toString()
            binding.textViewDateMonth.text = monthOfYear.toString()

            binding.textViewDateMonth.text = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())

            binding.textViewDateFullDate.text = (cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                ?.plus(" ") ?: " ").plus(dayOfMonth).plus(", ").plus(year)

        }, year, month, day)

        dpd.datePicker.minDate = System.currentTimeMillis()
        dpd.show()

    }

    private fun DatePicker.getDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        return calendar.time
    }

    private fun setDateTime(hour: Int, minute: Int) : String{
        val minuteOfHour = if (minute.toString().length < 2)
            "0".plus(minute.toString())
        else
            minute.toString()

        val justHour = if (hour.toString().length < 2)
            "0".plus(hour.toString())
        else
            hour.toString()

        return justHour.plus(minuteOfHour).convertHourMinutes(requireContext())!!
    }

    private fun showTimePicker(){

        val defaultHour = if (DateFormat.is24HourFormat(context))
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        else
            Calendar.getInstance().get(Calendar.HOUR)

        val defaultMin = Calendar.getInstance().get(Calendar.MINUTE)

        val date1: Date? = longToCalendar(viewModel.selectedDate?.time)?.time!!.convertForTimeCompare()
        val date2: Date? = longToCalendar(Calendar.getInstance().time.time)?.time!!.convertForTimeCompare()

        val picker = CustomTimePickerDialog(requireContext(),
            { _, hourOfDay, minute ->

                val minuteOfHour = if (minute.toString().length < 2)
                    "0".plus(minute.toString())
                else
                    minute.toString()

                val justHour = if (hourOfDay.toString().length < 2)
                    "0".plus(hourOfDay.toString())
                else
                    hourOfDay.toString()

                if (minute != 1) {

                    viewModel.dateTime.value = if (DateFormat.is24HourFormat(context))
                        justHour.plus(minuteOfHour).convertHourMinutes(requireContext())
                    else
                        justHour.plus(minuteOfHour).convertHourMinutes(requireContext()).convert24HourFormat()

                    binding.textViewDateTime.text = justHour.plus(minuteOfHour).convertHourMinutes(requireContext())
                }

            },
            defaultHour,
            defaultMin,
            DateFormat.is24HourFormat(context),
            1,
            date1,
            date2,
            R.style.SpinnerTimePickerDialog

        )
        picker.show()

    }

    private fun continueAfterMapInitialized() {
        if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onLocationPermissionOk() {

        /*if(isReport) {
            if(isStart) {

                viewModel.startLocationReport.value?.let {
                    val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                    val zoom = CameraUpdateFactory.zoomTo(14f)
                    googleMap.moveCamera(cu)
                    googleMap.animateCamera(zoom)
                    return
                }
            }
            else {
                viewModel.endLocationReport.value?.let {
                    val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                    val zoom = CameraUpdateFactory.zoomTo(14f)
                    googleMap.moveCamera(cu)
                    googleMap.animateCamera(zoom)
                    return
                }
            }
        }
        else {
            if(isStart) {

                viewModel.startLocationStart.value?.let {
                    val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                    val zoom = CameraUpdateFactory.zoomTo(14f)
                    googleMap.moveCamera(cu)
                    googleMap.animateCamera(zoom)
                    return
                }
            }
            else {
                viewModel.endLocationStart.value?.let {
                    val cu = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                    val zoom = CameraUpdateFactory.zoomTo(14f)
                    googleMap.moveCamera(cu)
                    googleMap.animateCamera(zoom)
                    return
                }
            }
        }*/

        locationClient = FusedLocationClient(requireContext())

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            override fun onLocationUpdated(location: Location) {

                locationClient.stop()

                val cu = CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16f)
                googleMap.moveCamera(cu)
                AppDataManager.instance.currentLocation = location
            }

            override fun onLocationFailed(message: String) {
                if(activity?.isFinishing != false || activity?.isDestroyed != false) {
                    return
                }

                when (message) {
                    FusedLocationClient.ERROR_LOCATION_DISABLED -> locationClient.showLocationSettingsDialog()
                    FusedLocationClient.ERROR_LOCATION_MODE -> {
                        locationClient.showLocationSettingsDialog()
                    }
                    FusedLocationClient.ERROR_TIMEOUT_OCCURRED -> {
                        (activity as BaseActivity<*>).handleError(RuntimeException(getString(R.string.location_timeout)))
                    }
                }
            }

        })
    }

    override fun onLocationPermissionFailed() {
    }

    fun continueAfterFromToSelected() {

        isFromAndToSelected = true

        viewModel.toLocation.value?.let { toLatLng ->

            viewModel.fromLocation.value?.let {  fromLatLng ->

                val minLat = if(fromLatLng.latitude < toLatLng.latitude) fromLatLng.latitude else toLatLng.latitude
                val maxLat = if(fromLatLng.latitude < toLatLng.latitude) toLatLng.latitude else fromLatLng.latitude
                val minLng = if(fromLatLng.longitude < toLatLng.longitude) fromLatLng.longitude else toLatLng.longitude
                val maxLng = if(fromLatLng.longitude < toLatLng.longitude) toLatLng.longitude else fromLatLng.longitude

                val cu = CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)), 200)
                googleMap.moveCamera(cu)

                binding.layoutTo.visibility = View.VISIBLE
                binding.imageViewMarkerCenter.visibility = View.GONE
                fromMarker?.remove()
                fromMarker = googleMap.addMarker(MarkerOptions().position(fromLatLng).icon(fromPinIcon))
                fromMarker?.isDraggable = true
                fromMarker?.title = "fromMarker"
                toMarker?.remove()
                toMarker = googleMap.addMarker(MarkerOptions().position(toLatLng).icon(toPinIcon))
                toMarker?.isDraggable = true
                toMarker?.title = "toMarker"
            }

        }

        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }
    private fun setupBackPressListener() {
        this.view?.isFocusableInTouchMode = true
        this.view?.requestFocus()
        this.view?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action != KeyEvent.ACTION_DOWN) {

                if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.isHideable = true
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else
                    activity?.finish()

                true
            } else
                false
        }

       }

    private fun continueAfterFromSelected() {

        if (viewModel.isFrom)
            binding.imageViewMarkerCenter.setImageResource(R.drawable.ic_map_pin_purple)
        else
            binding.imageViewMarkerCenter.setImageResource(R.drawable.ic_map_pin_marigold)

        viewModel.fromLocation.value?.let {
            fromMarker?.remove()
            fromMarker = googleMap.addMarker(MarkerOptions().position(it).icon(fromPinIcon))
            fromMarker?.isDraggable = true
            fromMarker?.title = "fromMarker"
        }

        binding.layoutTo.visibility = View.VISIBLE
        viewModel.isFrom = false
    }

    private fun createStoreMarker(@LayoutRes layoutId: Int): Bitmap {
        val markerView = layoutInflater.inflate(layoutId, null)

        markerView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

        val bitmap = Bitmap.createBitmap(markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)
        return bitmap
    }


    override fun getViewModel(): FlexirideViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[FlexirideViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "FlexiRideFromFragment"
        fun newInstance() = FlexiRideFromFragment()

    }

}