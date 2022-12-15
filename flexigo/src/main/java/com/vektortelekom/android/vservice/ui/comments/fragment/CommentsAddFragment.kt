package com.vektortelekom.android.vservice.ui.comments.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.vektor.ktx.utils.ImageHelper
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.CommentsAddFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.comments.CommentsViewModel
import com.vektortelekom.android.vservice.ui.comments.adapters.PhotoListAdapter
import com.vektortelekom.android.vservice.ui.dialog.CustomTimePickerDialog
import com.vektortelekom.android.vservice.utils.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class CommentsAddFragment : BaseFragment<CommentsViewModel>(), PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CommentsViewModel

    lateinit var binding: CommentsAddFragmentBinding

    private var currentSelect: SelectType? = null

    private var mPhotoFile = ""

    private val PICK_IMAGE_CAMERA = 2000

    private lateinit var photoListAdapter: PhotoListAdapter

    private var isMyRouteGet = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CommentsAddFragmentBinding>(inflater, R.layout.comments_add_fragment, container, false).apply {
            lifecycleOwner = this@CommentsAddFragment
            viewModel = this@CommentsAddFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(viewModel.ticketTypes.value == null) {
            viewModel.getTicketTypes(resources.configuration.locale.language)
        }

        if(viewModel.destinations.value == null) {
            viewModel.getDestinations()
        }

        val currentDate = Date()
        viewModel.selectedDate = currentDate

        val defaultHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val defaultMin = Calendar.getInstance().get(Calendar.MINUTE)

        viewModel.dateTime.value = setDateTime(defaultHour, defaultMin)

        binding.textViewDateTime.text = setDateTime(defaultHour, defaultMin)

        binding.textViewDateFullDate.text = viewModel.selectedDate.convertForBackend()

        binding.textViewDateFullDate.setOnClickListener {
            showDatePicker()
        }

        binding.textViewDateTime.setOnClickListener {
            showTimePicker()
        }

        binding.buttonSelectDemand.setOnClickListener {

            val ticketTypes = viewModel.ticketTypes.value

            if(ticketTypes != null && ticketTypes.isNotEmpty()) {

                currentSelect = SelectType.Demand

                val displayedValues = Array(ticketTypes.size) { "" }
                for(i in ticketTypes.indices) {
                    displayedValues[i] = ticketTypes[i].name
                }

                binding.numberPicker.value = 0
                binding.numberPicker.displayedValues = null
                binding.numberPicker.minValue = 0
                binding.numberPicker.maxValue = ticketTypes.size-1
                binding.numberPicker.displayedValues = displayedValues
                binding.numberPicker.value = viewModel.selectedTicketTypeIndex?:0
                binding.numberPicker.wrapSelectorWheel = true
                binding.textViewSelectTitle.text = getString(R.string.demand_type)

                binding.layoutSelect.visibility = View.VISIBLE

            }
            else {
                viewModel.navigator?.handleError(java.lang.Exception(getString(R.string.warning_select_demand_type_empty)))
            }
        }

        binding.cardViewDemandDate.setOnClickListener {
            SingleDateAndTimePickerDialog.Builder(context)
                    .defaultDate(viewModel.selectedDate?:Date())
                    .bottomSheet()
                    .minutesStep(1)
                    .curved()
                    .todayText(getString(R.string.today))
                    .title(getString(R.string.demand_date))
                    .listener { selectedDate ->
                        viewModel.selectedDate = selectedDate
                        binding.textViewDateFullDate.text = selectedDate.convertForBackend()
                    }
                    .customLocale(Locale("tr", "TR"))
                    .titleTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
                    .mainColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))
                    .display()
        }

        binding.buttonSelectLocation.setOnClickListener {

            val locations = viewModel.destinations.value

            if(locations != null && locations.isNotEmpty()) {

                currentSelect = SelectType.Location

                val displayedValues = Array(locations.size) { "" }
                for(i in locations.indices) {
                    displayedValues[i] = locations[i].title?:locations[i].name?:""
                }

                binding.numberPicker.value = 0
                binding.numberPicker.displayedValues = null
                binding.numberPicker.minValue = 0
                binding.numberPicker.maxValue = locations.size-1
                binding.numberPicker.displayedValues = displayedValues
                binding.numberPicker.value = viewModel.selectedDestinationIndex?:0
                binding.numberPicker.wrapSelectorWheel = true
                binding.textViewSelectTitle.text = getString(R.string.location)

                binding.layoutSelect.visibility = View.VISIBLE

            }
            else {
                viewModel.navigator?.handleError(java.lang.Exception(getString(R.string.warning_select_location_empty)))
            }

        }

        binding.buttonSelectRoute.setOnClickListener {

            if(viewModel.selectedDestination == null) {
                viewModel.navigator?.handleError(java.lang.Exception(getString(R.string.warning_select_location_first)))
            }
            else {

                val routes = viewModel.routes.value

                if(routes != null && routes.isNotEmpty()) {

                    currentSelect = SelectType.Route

                    val displayedValues = Array(routes.size) { "" }
                    for(i in routes.indices) {
                        displayedValues[i] = routes[i].name
                    }

                    binding.numberPicker.value = 0
                    binding.numberPicker.displayedValues = null
                    binding.numberPicker.minValue = 0
                    binding.numberPicker.maxValue = routes.size-1
                    binding.numberPicker.displayedValues = displayedValues
                    binding.numberPicker.value = viewModel.selectedRouteIndex?:0
                    binding.numberPicker.wrapSelectorWheel = true
                    binding.textViewSelectTitle.text = getString(R.string.route)

                    binding.layoutSelect.visibility = View.VISIBLE

                }
                else {
                    viewModel.navigator?.handleError(java.lang.Exception(getString(R.string.warning_select_route_empty)))
                }

            }

        }

        binding.buttonSelectCancel.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
        }

        binding.buttonSelectSelect.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
            when(currentSelect) {
                SelectType.Demand -> {
                    viewModel.selectedTicketType = viewModel.ticketTypes.value?.get(binding.numberPicker.value)
                    viewModel.selectedTicketTypeIndex = binding.numberPicker.value
                    binding.buttonSelectDemand.text = viewModel.selectedTicketType?.name
                }
                SelectType.Location -> {
                    viewModel.selectedDestination = viewModel.destinations.value?.get(binding.numberPicker.value)
                    viewModel.selectedDestinationIndex = binding.numberPicker.value
                    binding.buttonSelectLocation.text = viewModel.selectedDestination?.title
                    viewModel.selectedRoute = null
                    viewModel.selectedRouteIndex = 0
                    binding.buttonSelectRoute.text = getString(R.string.select)

                    viewModel.selectedDestination?.id?.let { it1 -> viewModel.getDestinationRoutes(it1) }
                }
                SelectType.Route -> {
                    viewModel.selectedRoute = viewModel.routes.value?.get(binding.numberPicker.value)
                    viewModel.selectedRouteIndex = binding.numberPicker.value
                    binding.buttonSelectRoute.text = viewModel.selectedRoute?.name
                }
                else -> {

                }
            }
        }

        val photoList = mutableListOf<String>()

        viewModel.fileUuids = photoList


        photoListAdapter = PhotoListAdapter(photoList, object: PhotoListAdapter.PhotoListener {
            override fun addPhotoClicked() {
                if(requireActivity() is BaseActivity<*> && (requireActivity() as BaseActivity<*>).checkAndRequestCameraPermission(this@CommentsAddFragment)) {
                    onCameraPermissionOk()
                }
            }

        })

        binding.recyclerViewPhotos.adapter = photoListAdapter

        viewModel.createTicketSuccess.observe(viewLifecycleOwner) { result ->
            if (result != null) {

                val builder = AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialogRounded).create()

                val viewDialog = layoutInflater.inflate(R.layout.message_dialog,null)
                val button = viewDialog.findViewById<Button>(R.id.other_button)
                val icon = viewDialog.findViewById<AppCompatImageView>(R.id.imageview_icon)
                val title = viewDialog.findViewById<TextView>(R.id.textview_subtitle)
                val subTitle = viewDialog.findViewById<TextView>(R.id.textview_title)

                subTitle.text = getString(R.string.feedback_message)
                title.text = getString(R.string.thank_you)

                icon.setBackgroundResource(R.drawable.ic_check)

                builder.setView(view)
                button.setOnClickListener {
                    builder.dismiss()
                    viewModel.navigator?.returnCommentsMainFragment(null)
                }
                builder.setCanceledOnTouchOutside(false)
                builder.show()

                viewModel.createTicketSuccess.value = null
            }
        }

        viewModel.destinations.observe(viewLifecycleOwner) { destinations ->
            AppDataManager.instance.personnelInfo?.destination?.let { myDestination ->
                destinations.forEachIndexed { index, destinationModel ->
                    if (destinationModel.id == myDestination.id) {
                        viewModel.selectedDestination = destinationModel
                        viewModel.selectedDestinationIndex = index
                        binding.buttonSelectLocation.text = viewModel.selectedDestination?.title

                        viewModel.selectedDestination?.id?.let { it1 -> viewModel.getDestinationRoutes(it1) }

                    }
                }
            }
            if (AppDataManager.instance.personnelInfo?.destination == null) {
                isMyRouteGet = true
            }
        }

        viewModel.routes.observe(viewLifecycleOwner) {
            if (isMyRouteGet.not()) {
                it.forEachIndexed { index, routeModel ->
                    if (routeModel.id == AppDataManager.instance.personnelInfo?.routeId) {
                        viewModel.selectedRoute = routeModel
                        viewModel.selectedRouteIndex = index
                        binding.buttonSelectRoute.text = viewModel.selectedRoute?.name
                    }
                }
                isMyRouteGet = true
            }
        }

    }

    private fun showTimePicker(){

        val defaultHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
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
                    viewModel.dateTime.value = justHour.plus(minuteOfHour).convertHourMinutes()
                    binding.textViewDateTime.text = justHour.plus(minuteOfHour).convertHourMinutes()
                }

            },
            defaultHour,
            defaultMin,
            true,
            1,
            date1,
            date2,
            R.style.SpinnerTimePickerDialog

        )
        picker.show()

    }

    private fun showDatePicker(){
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireActivity(), { view, _, _, _ ->

            viewModel.selectedDate = view.getDate()
            binding.textViewDateFullDate.text = viewModel.selectedDate.convertForBackend()

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

        return justHour.plus(minuteOfHour).convertHourMinutes()!!
    }

    override fun getViewModel(): CommentsViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CommentsViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CommentsAddFragment"

        fun newInstance() = CommentsAddFragment()

    }

    enum class SelectType {
        Demand,
        Location,
        Route
    }

    override fun onCameraPermissionOk() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri = setPhotoUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            intent.clipData = ClipData.newRawUri("", uri)
        }
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, PICK_IMAGE_CAMERA)
    }

    override fun onCameraPermissionFailed() {

    }

    private fun setPhotoUri(): Uri {
        val result = ImageHelper.getPhotoFile(requireContext())
        this.mPhotoFile = result.photoFile
        return result.photoUri!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PICK_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            viewModel.previewPhotoPath = mPhotoFile
            viewModel.navigator?.showPhotoPreviewFragment(null)
        }
    }

    fun addNewPhoto() {

        val activity = requireActivity()

        if(activity is BaseActivity<*>) {
            activity.showPd()
            GlideApp.with(requireContext()).asBitmap()
                    .load(mPhotoFile)
                    .apply(RequestOptions().override(960, 960))
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                            Timber.e(e, "Glide first resize failed: %s", model ?: "null")
                            if (e != null) {
                                for (t in e.rootCauses) {
                                    Timber.e(t, "Caused by")
                                }
                            }
                            activity.dismissPd()
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            // replace original image with smaller jpeg version
                            ImageHelper.saveBitmapAsJpeg(mPhotoFile, resource)

                            activity.runOnUiThread {
                                photoListAdapter.addPhoto(mPhotoFile)
                                activity.dismissPd()
                            }
                            return true
                        }
                    }).submit()
        }
    }

    fun takePhotoAgain() {
        onCameraPermissionOk()
    }

}