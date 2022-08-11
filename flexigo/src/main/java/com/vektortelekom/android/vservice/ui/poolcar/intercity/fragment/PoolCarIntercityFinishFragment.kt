package com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment

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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.vektor.ktx.utils.ImageHelper
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarIntercityFinishFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.poolcar.intercity.PoolCarIntercityViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import timber.log.Timber
import javax.inject.Inject

class PoolCarIntercityFinishFragment: BaseFragment<PoolCarIntercityViewModel>(), PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarIntercityViewModel

    lateinit var binding: PoolCarIntercityFinishFragmentBinding

    private val PICK_IMAGE_CAMERA = 1788

    private var mPhotoFile = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarIntercityFinishFragmentBinding>(inflater, R.layout.pool_car_intercity_finish_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarIntercityFinishFragment
            viewModel = this@PoolCarIntercityFinishFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardViewAddPhoto.setOnClickListener {
            if(requireActivity() is BaseActivity<*> && (requireActivity() as BaseActivity<*>).checkAndRequestCameraPermission(this@PoolCarIntercityFinishFragment)) {
                onCameraPermissionOk()
            }
        }

        viewModel.finishRentalResponse.observe(viewLifecycleOwner) {
            FlexigoInfoDialog.Builder(requireContext())
                    .setText1(getString(R.string.pool_car_intercity_finish_success))
                    .setCancelable(false)
                    .setIconVisibility(true)
                    .setOkButton(getString(R.string.Generic_Ok)) { dialog ->
                        dialog.dismiss()
                        activity?.finish()
                    }
                    .create()
                    .show()
        }

    }

    override fun getViewModel(): PoolCarIntercityViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarIntercityViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarIntercityFinishFragment"

        fun newInstance() = PoolCarIntercityFinishFragment()

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

                            activity.runOnUiThread {
                                viewModel.finishPhotoUuid.value = mPhotoFile
                                binding.cardViewTakenPhoto.visibility = View.VISIBLE
                                binding.imageViewTakenPhoto.setImageBitmap(resource)
                                activity.dismissPd()
                            }
                            return true
                        }
                    }).submit()
        }
    }

}