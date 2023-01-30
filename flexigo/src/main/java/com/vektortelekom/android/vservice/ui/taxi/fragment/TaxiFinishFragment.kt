package com.vektortelekom.android.vservice.ui.taxi.fragment

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
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
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.vektor.ktx.service.FusedLocationClient
import com.vektor.ktx.utils.ImageHelper
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.VektorEnum
import com.vektortelekom.android.vservice.databinding.TaxiFinishFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.taxi.TaxiViewModel
import com.vektortelekom.android.vservice.utils.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class TaxiFinishFragment: BaseFragment<TaxiViewModel>(), PermissionsUtils.CameraStateListener, PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: TaxiViewModel

    lateinit var binding: TaxiFinishFragmentBinding

    var selectedDate: Date? = null

    private var purposeTypes: List<VektorEnum>? = null

    private val PICK_IMAGE_CAMERA = 2000

    private var mPhotoFile = ""

    @Volatile
    private var myLocation: Location? = null
    private lateinit var locationClient: FusedLocationClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<TaxiFinishFragmentBinding>(inflater, R.layout.taxi_finish_fragment, container, false).apply {
            lifecycleOwner = this@TaxiFinishFragment
            viewModel = this@TaxiFinishFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getPurposes(getString(R.string.generic_language))

        val currentDate = Date()
        viewModel.selectedDateFinish = currentDate
        viewModel.selectedDateTextFinish = currentDate.convertForTicketFullDate(requireContext())

        binding.textViewDateFullDate.text = viewModel.selectedDateTextFinish
        binding.textViewDateDay.text = currentDate.convertForDay()
        binding.textViewDateMonth.text = currentDate.convertForMonth()

        binding.cardViewDemandDate.setOnClickListener {

            val builder = SingleDateAndTimePickerDialog.Builder(context)
                    .defaultDate(viewModel.selectedDateFinish?:Date())
                    .bottomSheet()
                    .minutesStep(1)
                    .curved()
                    .todayText(getString(R.string.today))
                    .title(getString(R.string.demand_date))
                    .listener { selectedDate ->
                        viewModel.selectedDateFinish = selectedDate
                        viewModel.selectedDateTextFinish = selectedDate.convertForTicketFullDate(requireContext())

                        binding.textViewDateFullDate.text = viewModel.selectedDateTextFinish
                        binding.textViewDateDay.text = selectedDate.convertForDay()
                        binding.textViewDateMonth.text = selectedDate.convertForMonth()
                    }
                    .customLocale(Locale("tr", "TR"))
                    .titleTextColor(ContextCompat.getColor(requireContext(), R.color.steel))
                    .mainColor(ContextCompat.getColor(requireContext(), R.color.darkNavyBlue))

            viewModel.taxiUsage.value?.usageDate?.let {
                try{
                    builder.minDateRange(Date(it.toLong()))
                }
                catch (e: java.lang.Exception) {

                }

            }

            builder.display()

        }

        binding.buttonPurposeOfUse.setOnClickListener {

            viewModel.purposes.value?.let {
                purposeTypes = it
            } ?: return@setOnClickListener




            if(purposeTypes != null && purposeTypes!!.isNotEmpty()) {

                val displayedValues = Array(purposeTypes!!.size) { "" }
                for(i in purposeTypes!!.indices) {
                    displayedValues[i] = purposeTypes!![i].text
                }

                binding.numberPicker.value = 0
                binding.numberPicker.displayedValues = null
                binding.numberPicker.minValue = 0
                binding.numberPicker.maxValue = purposeTypes!!.size-1
                binding.numberPicker.displayedValues = displayedValues
                binding.numberPicker.value = viewModel.selectedPurposeIndexFinish?:0
                binding.numberPicker.wrapSelectorWheel = true
                binding.textViewSelectTitle.text = getString(R.string.demand_type)

                binding.layoutSelect.visibility = View.VISIBLE

            }
            else {
                viewModel.navigator?.handleError(java.lang.Exception(getString(R.string.warning_select_demand_type_empty)))
            }
        }

        binding.buttonSelectCancel.setOnClickListener {
            binding.layoutSelect.visibility = View.GONE
        }

        binding.buttonSelectSelect.setOnClickListener {

            val purpose = purposeTypes?.get(binding.numberPicker.value)

            if(purpose?.value == "PERSONAL") {
                binding.textViewPay.visibility = View.GONE
                binding.editTextPay.visibility = View.GONE
                binding.textViewUploadPhoto.visibility = View.GONE
                binding.cardViewAddPhoto.visibility = View.GONE
            }
            else {
                binding.textViewPay.visibility = View.VISIBLE
                binding.editTextPay.visibility = View.VISIBLE
                binding.textViewUploadPhoto.visibility = View.VISIBLE
                binding.cardViewAddPhoto.visibility = View.VISIBLE
            }

            binding.layoutSelect.visibility = View.GONE
            viewModel.selectedPurposeTypeFinish = purpose
            viewModel.selectedPurposeIndexFinish = binding.numberPicker.value
            binding.buttonPurposeOfUse.text = viewModel.selectedPurposeTypeFinish?.text
        }

        binding.buttonCancel.setOnClickListener {
            activity?.finish()
        }

        binding.cardViewAddPhoto.setOnClickListener {

            if(requireActivity() is BaseActivity<*> && (requireActivity() as BaseActivity<*>).checkAndRequestCameraPermission(this@TaxiFinishFragment)) {
                onCameraPermissionOk()
            }

        }

        /*viewModel.taxiUsageCreated.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            FlexigoInfoDialog.Builder(requireContext())
                    .setIconVisibility(false)
                    .setTitle(getString(R.string.taxi_invoicing))
                    .setText1(getString(R.string.taxi_invoicing_success))
                    .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                        activity?.finish()
                    }
                    .create().show()

        })*/

        if (activity is BaseActivity<*> && (activity as BaseActivity<*>).checkAndRequestLocationPermission(this)) {
            onLocationPermissionOk()
        }


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
            addNewPhoto()
        }
    }

    private fun addNewPhoto() {

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
                            viewModel.finishPhotoUrl = mPhotoFile

                            activity.runOnUiThread {
                                binding.layoutAddPhoto.visibility = View.GONE
                                binding.layoutAddPhoto.post {
                                    binding.imageViewPhoto.setImageBitmap(resource)
                                }
                                activity.dismissPd()
                            }
                            return true
                        }
                    }).submit()
        }
    }

    override fun getViewModel(): TaxiViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[TaxiViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "TaxiFinishFragment"
        fun newInstance() = TaxiFinishFragment()
    }

    override fun onLocationPermissionOk() {
        locationClient = FusedLocationClient(requireContext())

        locationClient.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationClient.start(20 * 1000, object : FusedLocationClient.FusedLocationCallback {
            override fun onLocationUpdated(location: Location) {

                myLocation = location

                if(viewModel.isEndLocationChangedManuelly.not()) {
                    viewModel.endLocationFinish.value = LatLng(location.latitude, location.longitude)

                    val geoCoder = Geocoder(requireContext(), Locale("tr-TR"))

                    try {
                        val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)

                        if(addresses.size > 0) {
                            val address = addresses[0]

                            viewModel.endLocationTextFinish.value = address.getAddressLine(0)

                        }
                    }
                    catch (e: Exception) {
                        viewModel.endLocationTextFinish.value = ""
                    }
                }

                locationClient.stop()


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

}