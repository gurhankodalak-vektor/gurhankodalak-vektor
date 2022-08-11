package com.vektortelekom.android.vservice.ui.base.photo

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import javax.inject.Inject

class TakePhotoActivity : BaseActivity<TakePhotoViewModel>(),
        TakePhotoNavigator,
        PermissionsUtils.CameraStateListener{

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: TakePhotoViewModel

    private var isPreviewShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.take_photo_activity)

        viewModel.navigator = this

        val desc = intent.getStringExtra("description")

        if(desc == null) {
            viewModel.descriptionText.value = "Ehliyetinizin ön yüzünün net bir fotoğrafını ekleyiniz."
        }
        else {
            viewModel.descriptionText.value = desc
        }

        val previewAcceptText = intent.getStringExtra("previewAcceptText")

        if(previewAcceptText == null) {
            viewModel.previewAcceptText.value = "Fotoğrafı Yükle"
        }
        else {
            viewModel.previewAcceptText.value = previewAcceptText
        }

        val previewRejectText = intent.getStringExtra("previewRejectText")

        if(previewRejectText == null) {
            viewModel.previewRejectText.value = "Yeniden Çek"
        }
        else {
            viewModel.previewRejectText.value = previewRejectText
        }

        viewModel.mPhotoFile = intent.getStringExtra("photoFile")

        /*if(mPhotoFile == null) {
            setPhotoUri()
        }*/

        showTakePhotoFragment(null)

    }

    override fun getViewModel(): TakePhotoViewModel {
        viewModel = ViewModelProvider(this, factory)[TakePhotoViewModel::class.java]
        return viewModel
    }

    override fun onBackPressed() {
        if(isPreviewShowing) {
            showTakePhotoFragment(null)
        }
        else {
            finish()
        }
    }

    override fun onCameraPermissionOk() {
    }

    override fun onCameraPermissionFailed() {
    }

    override fun showViewPhotoFragment(view: View?) {
        isPreviewShowing = true
        replaceFragment(R.id.root_view, TakePhotoPreviewFragment.newInstance(), TakePhotoPreviewFragment.TAG)
    }

    override fun showTakePhotoFragment(view: View?) {
        isPreviewShowing = false
        viewModel.bitmapPhoto = null
        replaceFragment(R.id.root_view, TakePhotoFragment.newInstance(), TakePhotoFragment.TAG)
    }

}
