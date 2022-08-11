package com.vektortelekom.android.vservice.ui.base.photo

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.TakePhotoFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import timber.log.Timber
import javax.inject.Inject

class TakePhotoFragment(val aspectRatioX: Int, val aspectRatioY : Int, val minWidth: Int, val minHeight: Int) : BaseFragment<TakePhotoViewModel>(),
        PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: TakePhotoViewModel
    private lateinit var binding: TakePhotoFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate<TakePhotoFragmentBinding>(inflater, R.layout.take_photo_fragment, container, false).apply {
            lifecycleOwner = this@TakePhotoFragment
            viewModel = this@TakePhotoFragment.viewModel
        }

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.camera.setLifecycleOwner(this)
        CameraLogger.registerLogger { level, _, message, throwable -> Timber.log(level + 3, throwable, message) }
        if (BuildConfig.DEBUG) {
            CameraLogger.setLogLevel(CameraLogger.LEVEL_WARNING)
        } else {
            CameraLogger.setLogLevel(CameraLogger.LEVEL_WARNING)
        }
        binding.camera.isEnabled = false
        binding.camera.audio = Audio.OFF

        val width = SizeSelectors.minWidth(minWidth)
        val height = SizeSelectors.minHeight(minHeight)
        val dimensions = SizeSelectors.and(width, height)
        val ratio = SizeSelectors.aspectRatio(AspectRatio.of(aspectRatioX, aspectRatioY), 0f)
        val result = SizeSelectors.or(
                SizeSelectors.and(ratio, dimensions), // Try to match both constraints
                ratio, // If none is found, at least try to match the aspect ratio
                SizeSelectors.biggest() // If none is found, take the biggest
        )
        binding.camera.setPictureSize(result)

        binding.camera.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                result.toBitmap(640, 640) { bitmap -> onImageCaptured(bitmap) }
            }
        })

        binding.captureCamera.setOnClickListener(View.OnClickListener {
            if (binding.camera.isTakingPicture)
                return@OnClickListener

            binding.camera.takePictureSnapshot()
        })

    }


    fun onImageCaptured(image: Bitmap?) {
        if (image == null) {
            AppLogger.w("Bitmap is null")
            return
        }

        activity?.let {

            viewModel.bitmapPhoto = image
            viewModel.navigator?.showViewPhotoFragment(null)
        }
    }



    companion object {
        const val TAG: String = "TakePhotoFragment"

        fun newInstance(aspectRatioX: Int = 1, aspectRatioY : Int = 1, minWidth: Int = 1280, minHeight: Int = 720): TakePhotoFragment {
            return TakePhotoFragment(aspectRatioX, aspectRatioY, minWidth, minHeight)
        }
    }

    override fun getViewModel(): TakePhotoViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[TakePhotoViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    override fun onCameraPermissionOk() {
    }

    override fun onCameraPermissionFailed() {
    }

}