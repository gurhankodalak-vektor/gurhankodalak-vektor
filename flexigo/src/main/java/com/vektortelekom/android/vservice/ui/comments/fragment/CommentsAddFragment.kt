package com.vektortelekom.android.vservice.ui.comments.fragment

import android.app.Activity
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
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.utils.GlideApp
import com.vektortelekom.android.vservice.utils.convertForDay
import com.vektortelekom.android.vservice.utils.convertForMonth
import com.vektortelekom.android.vservice.utils.convertForTicketFullDate
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
            viewModel.getTicketTypes()
        }

        if(viewModel.destinations.value == null) {
            viewModel.getDestinations()
        }

        val currentDate = Date()
        viewModel.selectedDate = currentDate

        binding.textViewDateFullDate.text = currentDate.convertForTicketFullDate()
        binding.textViewDateDay.text = currentDate.convertForDay()
        binding.textViewDateMonth.text = currentDate.convertForMonth()

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

                        binding.textViewDateFullDate.text = selectedDate.convertForTicketFullDate()
                        binding.textViewDateDay.text = selectedDate.convertForDay()
                        binding.textViewDateMonth.text = selectedDate.convertForMonth()
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

                val dialog = AppDialog.Builder(requireContext())
                        .setIconVisibility(true)
                        .setTitle(R.string.dialog_message_add_ticket_success)
                        .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                            viewModel.navigator?.returnCommentsMainFragment(null)
                        }
                        .create()

                dialog.show()

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