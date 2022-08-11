package com.vektortelekom.android.vservice.ui.base.photo

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.vektortelekom.android.vservice.ui.base.BaseViewModel
import javax.inject.Inject

class TakePhotoViewModel @Inject
constructor() : BaseViewModel<TakePhotoNavigator>() {

    var mPhotoFile: String? = null

    var bitmapPhoto: Bitmap? = null

    val descriptionText : MutableLiveData<String> = MutableLiveData()

    val previewAcceptText : MutableLiveData<String> = MutableLiveData()

    val previewRejectText : MutableLiveData<String> = MutableLiveData()

}