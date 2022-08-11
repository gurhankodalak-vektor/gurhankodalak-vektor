package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.vektor.ktx.utils.ImageHelper
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.ktx.utils.logger.AppLogger
import com.vektor.vshare_api_ktx.model.DoorStatus
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DeviceType
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.PoolCarRentalFinishParkInfoFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.ImageZoomDialog
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.ui.poolcar.dialog.DoorsOpeningDialog
import com.vektortelekom.android.vservice.ui.poolcar.dialog.DoorsOpeningNoDeviceDialog
import com.vektortelekom.android.vservice.utils.GlideApp
import org.joda.time.DateTime
import org.joda.time.Seconds
import timber.log.Timber
import javax.inject.Inject

class PoolCarRentalFinishParkInfoFragment: BaseFragment<PoolCarViewModel>(), PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    lateinit var binding: PoolCarRentalFinishParkInfoFragmentBinding

    private var mPhotoFile = ""

    private val PICK_IMAGE_CAMERA = 2007

    private val maxDuration: Long = 90_000
    private var startTime: DateTime? = null
    private var watchDuration: Long = maxDuration
    private var isOperationFailedWithTimeout: Boolean = false
    private var isWatcherTimerStarted: Boolean = false
    private var watcherTimer: CountDownTimer? = null
    private var requestTimer: CountDownTimer? = null

    private var doorsOpeningDialog: DoorsOpeningDialog? = null

    var handler: Handler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarRentalFinishParkInfoFragmentBinding>(inflater, R.layout.pool_car_rental_finish_park_info_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarRentalFinishParkInfoFragment
            viewModel = this@PoolCarRentalFinishParkInfoFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        binding.cardViewAddPhoto.setOnClickListener {
            if(requireActivity() is BaseActivity<*> && (requireActivity() as BaseActivity<*>).checkAndRequestCameraPermission(this@PoolCarRentalFinishParkInfoFragment)) {
                onCameraPermissionOk()
            }

        }

        binding.imageViewDeleteParkPhoto.setOnClickListener {

            viewModel.finishParkInfoPreviewPhotoPath = null
            binding.cardViewAddPhoto.visibility = View.VISIBLE
            binding.layoutTakenPhoto.visibility = View.GONE

        }

        binding.imageViewParkPhoto.setOnClickListener {

            val imageZoomDialog = viewModel.finishParkInfoPreviewPhotoPath?.let { it1 -> ImageZoomDialog(requireContext(), AppApiHelper(), it1, true) }
            imageZoomDialog?.show()

        }

        viewModel.finishRentalResponse.observe(viewLifecycleOwner) {

            if (viewModel.selectedVehicle.value?.deviceType == DeviceType.REMOTE_DOOR) {
                startTime = DateTime.now()
                watchDuration = maxDuration
                checkDoorStatus()
                if (doorsOpeningDialog == null) {
                    doorsOpeningDialog = DoorsOpeningDialog(requireContext(), false)
                    doorsOpeningDialog?.show()
                }
            } else {
                val dialog = DoorsOpeningNoDeviceDialog(requireContext(), false)
                dialog.show()

                handler?.postDelayed({
                    dialog.dismiss()
                    viewModel.navigator?.showPoolCarSatisfactionSurveyFragment()
                }, 6000)
            }


        }

        viewModel.checkDoorResponse.observe(viewLifecycleOwner, Observer { response ->

            if(response == null) {
                return@Observer
            }

            if (response.status == DoorStatus.CLOSED.toString()) {
                watcherTimer?.cancel()
                isWatcherTimerStarted = false
                var diff = 3000
                startTime?.let {
                    val time = it.plusSeconds(3)
                    diff = Seconds.secondsBetween(DateTime.now(), time).seconds * 1000
                    AppLogger.d("diff: $diff")
                }
                if (diff >= 1000) {
                    Handler().postDelayed({
                        doorsOpeningDialog?.dismiss()
                        viewModel.navigator?.showPoolCarSatisfactionSurveyFragment()
                    }, diff.toLong())
                } else {
                    doorsOpeningDialog?.dismiss()
                    viewModel.navigator?.showPoolCarSatisfactionSurveyFragment()
                }
            } else {
                requestTimer?.start()
            }
        })

        requestTimer = object : CountDownTimer(3_000, 3_000) {
            override fun onTick(l: Long) {
            }

            override fun onFinish() {
                AppLogger.d("requestTimer.onFinish")
                checkDoorStatus()
                requestTimer?.cancel()
            }
        }

    }

    private fun checkDoorStatus() {
        if (!isOperationFailedWithTimeout) {
            if (!isWatcherTimerStarted) {
                watcherTimer = object : CountDownTimer(watchDuration, 1_000) {
                    override fun onTick(l: Long) {
                        AppLogger.d("watcherTimer.onTick")
                    }

                    override fun onFinish() {
                        AppLogger.d("watcherTimer.onFinish")
                        isOperationFailedWithTimeout = true
                    }
                }
                watcherTimer?.start()
                AppLogger.d("watcherTimer?.start()")
                isWatcherTimerStarted = true
            }
            viewModel.checkDoorStatus()
        } else {
            isWatcherTimerStarted = false
            viewModel.navigator?.showNotClosedDoorDialog()
        }
    }

    fun takePhotoAgain() {
        onCameraPermissionOk()
    }

    fun addNewPhoto() {

        val activity = requireActivity()

        if(activity is BaseActivity<*>) {
            activity.showPd()
            GlideApp.with(requireActivity()).asBitmap()
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
                                binding.cardViewAddPhoto.visibility = View.GONE
                                binding.layoutTakenPhoto.visibility = View.VISIBLE
                                binding.imageViewParkPhoto.setImageBitmap(resource)
                                activity.dismissPd()
                            }
                            return true
                        }
                    }).submit()
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
            viewModel.finishParkInfoPreviewPhotoPath = mPhotoFile
            viewModel.navigator?.showPoolCarRentalFinishParkPhotoPreviewFragment(null)
        }
    }

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarRentalFinishParkInfoFragment"

        fun newInstance() = PoolCarRentalFinishParkInfoFragment()

    }

}