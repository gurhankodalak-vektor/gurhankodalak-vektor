package com.vektortelekom.android.vservice.ui.base

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.vektor.ktx.utils.ImageHelper
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import timber.log.Timber
import java.io.File
import java.util.*

abstract class PhotoUploadFragment<T : ViewModel> : BaseFragment<T>(), PermissionsUtils.CameraStateListener {

    private val PICK_IMAGE_CAMERA = 2000
    private var selectedImage: Int = 1
    private var selectedImages = ArrayList<String>()
    var uploadImagesIds: List<Int> = ArrayList()
    var uploadImagesUuids: List<String> = ArrayList()
    private var mPhotoFile = ""

    private fun processImage(uri: String) {
        if (uri.isEmpty()) {
            Timber.w("Uri is empty")
            showWarningPopup()
            return
        }

        try {
            val file = File(uri)
            if (!file.exists()) {
                Timber.e("Glide file not found: %s", uri)
                showWarningPopup()
                return
            }
        } catch (t: Throwable) {
            Timber.e(t, "processImage file control failed.")
        }

        val baseActivity = activity as? BaseActivity<*>
        baseActivity?.showPd()

        Glide.with(context?.applicationContext!!).asBitmap()
                .load(uri)
                .apply(RequestOptions().override(960, 960))
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                        Timber.e(e, "Glide first resize failed: %s", model ?: "null")
                        if (e != null) {
                            for (t in e.rootCauses) {
                                Timber.e(t, "Caused by")
                            }
                        }
                        baseActivity?.dismissPd()
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        // replace original image with smaller jpeg version
                        ImageHelper.saveBitmapAsJpeg(uri, resource)
                        activity?.runOnUiThread { onPhotoReady() }
                        baseActivity?.dismissPd()
                        return true
                    }
                }).submit()
    }

    open fun setAvatarImageToView(image: Bitmap) {}

    open fun onPhotoReady() {}

    private fun setPhotoUri(): Uri {
        val result = ImageHelper.getPhotoFile(context?.applicationContext!!)
        this.mPhotoFile = result.photoFile
        return result.photoUri!!
    }

    private fun showWarningPopup() {
        val baseActivity = activity as? BaseActivity<*>
        baseActivity?.handleError(RuntimeException(getString(R.string.photo_uri_not_found)))
    }

    fun startCameraActivity(activity: Activity) {

        val baseActivity = activity as? BaseActivity<*>

        if(baseActivity?.checkAndRequestCameraPermission(this)?.not()!!) {
            return
        }

        onCameraPermissionOk()
    }

    private fun openImageCaptureActivity() {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        val uri = setPhotoUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            intent.clipData = ClipData.newRawUri("", uri)
        }
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, PICK_IMAGE_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onCameraPermissionOk() {
        AppLogger.i("onCameraPermissionOk")

        openImageCaptureActivity()
    }

    override fun onCameraPermissionFailed() {
        AppLogger.i("onCameraPermissionFailed")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_CAMERA) {
            // add successful photo file to the list
            if (selectedImages.size >= selectedImage) {
                selectedImages.removeAt(selectedImage - 1)
                selectedImages.add(this.mPhotoFile)
            } else {
                selectedImages.add(this.mPhotoFile)
            }
            processImage(this.mPhotoFile)
        }
    }
}