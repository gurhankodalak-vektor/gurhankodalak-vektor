package com.vektortelekom.android.vservice.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.vektor.ktx.data.remote.ApiHelper
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import timber.log.Timber
import java.io.*


object PhotoHelper {

    private val requestOptions: RequestOptions
        get() {
            val requestOptions = RequestOptions()
//            requestOptions.placeholder(R.drawable.ic_car_icon)
//            requestOptions.error(R.drawable.ic_car_icon)
            return requestOptions
        }

    @SuppressLint("CheckResult")
    fun loadImageToImageView(apiHelper: ApiHelper, context: Context, fileUuid: String, imageView: ImageView, isPng: Boolean, placeHolderIcon: Int = R.drawable.ic_car_icon) {
        val url: String = if (isPng)
            "${apiHelper.baseUrl2}/report/fileViewer/uuid/$fileUuid.png"
        else
            "${apiHelper.baseUrl2}/report/fileViewer/uuid/$fileUuid"

        try {
            AppLogger.i("url=$url")

            requestOptions.placeholder(placeHolderIcon)
            requestOptions.error(placeHolderIcon)

            Glide.with(context).setDefaultRequestOptions(requestOptions).load(url).into(imageView)
        } catch (e: Exception) {
            Timber.e(e, "Error uploadImageToImageView")
        }
    }

    @SuppressLint("CheckResult")
    fun loadImageToImageView(context: Context, imageFileUrl: String, imageView: ImageView, placeHolderIcon: Int = R.drawable.ic_car_icon) {

        try {
            AppLogger.i("fileUrl=$imageFileUrl")

            requestOptions.placeholder(placeHolderIcon)
            requestOptions.error(placeHolderIcon)

            Glide.with(context).setDefaultRequestOptions(requestOptions).load(imageFileUrl).into(imageView)
        } catch (e: Exception) {
            Timber.e(e, "Error uploadImageToImageView")
        }
    }

    fun getPhotoFromGalleryUri(uriFrom: Uri, urlTo: String, context: Context, listener: GalleryTaskListener) {

        object: AsyncTask<Void, Void, Boolean>() {
            override fun doInBackground(vararg params: Void?): Boolean {

                try {
                    val inputStream = context.contentResolver.openInputStream(uriFrom)
                    val file = File(urlTo)
                    copy(inputStream!!, file)
                    return true
                }
                catch (exception: java.lang.Exception) {
                    return false
                }

            }

            override fun onPostExecute(result: Boolean?) {
                listener.downloadFromGalleryCompleted(urlTo, result == true)
            }

            @Throws(IOException::class)
            fun copy(inputStream: InputStream, dst: File) {
                try {
                    val out: OutputStream = FileOutputStream(dst)
                    try {
                        // Transfer bytes from in to out
                        val buf = ByteArray(1024)
                        var len: Int
                        while (inputStream.read(buf).also { len = it } > 0) {
                            out.write(buf, 0, len)
                        }
                    } finally {
                        out.close()
                    }
                } finally {
                    inputStream.close()
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    }

    @SuppressLint("CheckResult")
    fun loadCarImageToImageViewWithCache(context: Context, carImageName: String, imageView: ImageView, isPng: Boolean, placeHolderIcon: Int = R.drawable.ic_car_icon) {
        val url: String = if (isPng)
            "${BuildConfig.BASE_URL}/files/tiktak/cars/$carImageName.png"
        else
            "${BuildConfig.BASE_URL}/files/tiktak/cars/$carImageName"

        try {

            requestOptions.placeholder(placeHolderIcon)
            requestOptions.error(placeHolderIcon)

            Glide.with(context).setDefaultRequestOptions(requestOptions).load(url).diskCacheStrategy(DiskCacheStrategy.DATA).into(imageView)
        } catch (e: Exception) {
            Timber.e(e, "Error loadCarImageToImageViewWithCache")
        }

    }

    interface GalleryTaskListener {
        fun downloadFromGalleryCompleted(url: String, isSuccess: Boolean)
    }



}