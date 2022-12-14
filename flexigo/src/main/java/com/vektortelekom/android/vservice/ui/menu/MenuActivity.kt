package com.vektortelekom.android.vservice.ui.menu

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.vektor.ktx.utils.ImageHelper
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.MenuActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.login.fragment.ForgotPasswordFragment
import com.vektortelekom.android.vservice.ui.menu.fragment.*
import com.vektortelekom.android.vservice.ui.notification.NotificationActivity
import com.vektortelekom.android.vservice.ui.route.RouteSelectionActivity
import com.vektortelekom.android.vservice.ui.survey.bottomsheet.BottomSheetCommuteOptions
import com.vektortelekom.android.vservice.utils.AppConstants
import com.vektortelekom.android.vservice.utils.GlideApp
import org.joda.time.DateTime
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MenuActivity : BaseActivity<MenuViewModel>(), MenuNavigator, PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    private lateinit var binding: MenuActivityBinding

    private val PICK_IMAGE_CAMERA = 2001

    private var mPhotoFile = ""

    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<MenuActivityBinding>(this, R.layout.menu_activity).apply {
            lifecycleOwner = this@MenuActivity
            viewModel = this@MenuActivity.viewModel
        }
        viewModel.navigator = this

        viewModel.isAddressNotValid.value = intent.getBooleanExtra("is_address_not_valid", false)

        viewModel.isPoolCarActive = intent.getBooleanExtra("is_pool_car_active", false)
        viewModel.isShowDrivingLicence = intent.getBooleanExtra("is_show_driving_licence", false)

        viewModel.isForDrivingLicense = intent.getBooleanExtra("is_for_driving_license", false)

        viewModel.isComingSurvey = intent.getBooleanExtra("is_coming_survey", false)
        viewModel.isComingRegistration = intent.getBooleanExtra("is_coming_registration", false)
        viewModel.isLocationPermissionSuccess = intent.getBooleanExtra("is_location_permission_success", false)

        if(viewModel.isForDrivingLicense) {
            viewModel.getCustomerStatus()
        } else if(viewModel.isComingSurvey || viewModel.isComingRegistration) {
            showMenuAddressesFragment(null)
        } else {
            showMenuMainFragment(null)
        }

        viewModel.logoutResponse.observe(this) {
            viewModel.logoutUmng()
        }

        viewModel.logutUmngResponse.observe(this) {
            stateManager.logout()
            AppDataManager.instance.logout()
            showLoginActivity()
            finishAffinity()
        }

        viewModel.latestDrivingLicenceDocument.observe(this) { response ->
            if (response == null) {
                showMenuDrivingLicenseDialog(null)
            } else {
                showMenuDrivingLicensePreviewFragment(null)
            }
        }

        viewModel.customerStatus.observe(this) {
            if (viewModel.isForDrivingLicense) {
                viewModel.customerStatus.value?.user?.accountId?.let {
                    viewModel.getLatestDrivingLicenceDocument(it.toString())
                }
            }
        }
    }

    override fun showMenuMainFragment(view: View?) {
        binding.imageViewProfile.visibility = View.VISIBLE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, MenuMainFragment.newInstance(), MenuMainFragment.TAG)
                .commit()
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun onBackPressed() {
        if(supportFragmentManager.findFragmentByTag(MenuAddAddressFragment.TAG) != null && viewModel.isAddressNotValid.value == true) {
            finishAffinity()
        }
        if(viewModel.isForDrivingLicense) {
            finish()
        }
        if(supportFragmentManager.findFragmentByTag(ForgotPasswordFragment.TAG) != null) {
            // one more back pressed
            super.onBackPressed()
        }
        else if(supportFragmentManager.findFragmentByTag(MenuPdfViewerFragment.TAG) != null) {
            binding.textViewTitle.text = getString(R.string.settings)
            super.onBackPressed()
        }
        else if(supportFragmentManager.findFragmentByTag(MenuDrivingLicencePreviewFragment.TAG) != null) {
            binding.textViewTitle.text = getString(R.string.settings)
            super.onBackPressed()
        }
        else {
            super.onBackPressed()
            if(supportFragmentManager.findFragmentByTag(MenuEditProfileFragment.TAG) == null) {
                binding.textViewTitle.text = getString(R.string.settings)
            }

        }
    }

    override fun showMenuEditProfileFragment(view: View?) {
        binding.textViewTitle.text = getString(R.string.menu_edit_profile)
        binding.imageViewProfile.visibility = View.VISIBLE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, MenuEditProfileFragment.newInstance(), MenuEditProfileFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showMenuChangePasswordFragment(view: View?) {
        binding.textViewTitle.text = getString(R.string.menu_change_password)
        binding.imageViewProfile.visibility = View.VISIBLE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, MenuChangePasswordFragment.newInstance(), MenuChangePasswordFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showMenuCompanyInfoFragment(view: View?) {

    }

    override fun showMenuAddressesFragment(view: View?) {
        binding.textViewTitle.text = getString(R.string.menu_home_address)
        binding.imageViewProfile.visibility = View.GONE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, MenuAddAddressFragment.newInstance(), MenuAddAddressFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showMenuSettingsFragment(view: View?) {
        binding.imageViewProfile.visibility = View.VISIBLE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, MenuSettingsFragment.newInstance(), MenuSettingsFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showMenuPaymentFragment(view: View?) {

    }

    override fun showMenuSignOutFragment(view: View?) {

        AppDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setSubtitle(getString(R.string.logout_message))
                .setOkButton(getString(R.string.logout)) {
                    viewModel.logout()
                }
                .create()
                .show()
    }

    override fun returnMenuMainFragment() {
        supportFragmentManager.popBackStack()
    }

    override fun showPrivacyPolicyPdf(view: View?) {
        binding.textViewTitle.text = getString(R.string.privacy_policy)
        binding.imageViewProfile.visibility = View.VISIBLE

        val url = if(BuildConfig.FLAVOR == "tums") AppConstants.Documents.KVKK_TUMS else AppConstants.Documents.CONFIDENTIALITY_AGREEMENT

        val fragment = MenuPdfViewerFragment.newInstance()
        val bundle = Bundle()
//        bundle.putString("url", AppDataManager.instance.personnelInfo?.company?.rulesDocUrl)
        bundle.putString("url", url)
        fragment.arguments = bundle

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, MenuPdfViewerFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showSecurityPdf(view: View?) {
        binding.textViewTitle.text = getString(R.string.security)
        binding.imageViewProfile.visibility = View.VISIBLE

        val url = if(BuildConfig.FLAVOR == "tums") AppConstants.Documents.KVKK_TUMS else AppConstants.Documents.CONFIDENTIALITY_AGREEMENT

        val fragment = MenuPdfViewerFragment.newInstance()
        val bundle = Bundle()
//        bundle.putString("url", AppDataManager.instance.personnelInfo?.company?.securityDocUrl)
        bundle.putString("url", url)
        fragment.arguments = bundle

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, MenuPdfViewerFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showFlexigoPdf(view: View?) {
        binding.textViewTitle.text = getString(R.string.flexigo)
        binding.imageViewProfile.visibility = View.VISIBLE

        val url = if(BuildConfig.FLAVOR == "tums") AppConstants.Documents.KVKK_TUMS else AppConstants.Documents.CONFIDENTIALITY_AGREEMENT

        val fragment = MenuPdfViewerFragment.newInstance()
        val bundle = Bundle()
        bundle.putString("url", url)
        fragment.arguments = bundle

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, MenuPdfViewerFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showAboutApplicationPdf(view: View?) {
        binding.textViewTitle.text = getString(R.string.about_application)
        binding.imageViewProfile.visibility = View.VISIBLE

        val url = if(BuildConfig.FLAVOR == "tums") AppConstants.Documents.KVKK_TUMS else AppConstants.Documents.CONFIDENTIALITY_AGREEMENT

        val fragment = MenuPdfViewerFragment.newInstance()
        val bundle = Bundle()
        bundle.putString("url", url)
        fragment.arguments = bundle

        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, fragment, MenuPdfViewerFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun takePhoto(view: View?) {
        if(checkAndRequestCameraPermission(this)) {
            onCameraPermissionOk()
        }
    }

    override fun closePhotoPreviewFragment(view: View?) {
        binding.layoutToolbar.visibility = View.VISIBLE
        onBackPressed()
    }

    override fun useTakenPhoto(view: View?) {
        binding.layoutToolbar.visibility = View.VISIBLE
        if(supportFragmentManager.findFragmentByTag(MenuProfilePhotoPreviewFragment.TAG) != null) {
            onBackPressed()
        }

        GlideApp.with(this).asBitmap()
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
                        dismissPd()
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        // replace original image with smaller jpeg version
                        ImageHelper.saveBitmapAsJpeg(mPhotoFile, resource)

                        runOnUiThread {
                            dismissPd()
                            viewModel.changeProfilePhoto(mPhotoFile)
                        }
                        return true
                    }
                }).submit()


    }

    override fun takePhotoAgain(view: View?) {
        if(supportFragmentManager.findFragmentByTag(MenuProfilePhotoPreviewFragment.TAG) != null) {
            onBackPressed()
        }

        onCameraPermissionOk()
    }

    override fun profilePhotoUpdated(photoUuid: String) {
        val dialog = AppDialog.Builder(this)
                .setIconVisibility(true)
                .setTitle(R.string.change_photo_success)
                .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                    dialog.dismiss()
                }
                .create()

        dialog.show()
    }

    override fun showNotifications(view: View?) {
        startActivity(Intent(this@MenuActivity, NotificationActivity::class.java))
    }

    override fun showMenuHighlightDialog(view: View?) {
        FlexigoInfoDialog.Builder(this)
                .setIconVisibility(false)
                .setTitle(getString(R.string.highlight_restart_title))
                .setText1(getString(R.string.highlight_restart_text))
                .setOkButton(getString(R.string.Generic_Ok)) {
                    AppDataManager.instance.restartHighlights()
                    finish()

                }
                .setCancelButton(getString(R.string.cancel)) { dialog ->
                    dialog.dismiss()
                }
                .create().show()
    }

    override fun showMenuQuestionnaireDialog(view: View?) {
        binding.textViewTitle.text = getString(R.string.menu_questionnaire)
        binding.imageViewProfile.visibility = View.VISIBLE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, MenuQuestionnaireFragment.newInstance(), MenuQuestionnaireFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun showMenuDrivingLicenseDialog(view: View?) {
        binding.textViewTitle.text = getString(R.string.menu_driving_license)
        binding.imageViewProfile.visibility = View.VISIBLE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, MenuDrivingLicenseFragment.newInstance(), MenuDrivingLicenseFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun getViewModel(): MenuViewModel {
        viewModel = ViewModelProvider(this, factory)[MenuViewModel::class.java]
        return viewModel
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (fragment in supportFragmentManager.fragments) {
            if(fragment is PermissionsUtils.StateListener) {
                PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, fragment as PermissionsUtils.StateListener)
            }
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

    override fun showMenuDrivingLicensePreviewFragment(view: View?) {
        binding.textViewTitle.text = getString(R.string.menu_driving_license)
        binding.imageViewProfile.visibility = View.VISIBLE
        supportFragmentManager
            .beginTransaction()
            .add(R.id.root_fragment, MenuDrivingLicencePreviewFragment.newInstance(), MenuDrivingLicencePreviewFragment.TAG)
            .addToBackStack(null)
            .commit()
    }

    override fun onCameraPermissionFailed() {

    }

    private fun setPhotoUri(): Uri {
        val result = ImageHelper.getPhotoFile(this)
        this.mPhotoFile = result.photoFile
        return result.photoUri!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_CAMERA) {
            if(resultCode == Activity.RESULT_OK) {
                viewModel.previewPhotoPath = mPhotoFile
                showPhotoPreviewFragment()
            }
        }
    }

    private fun showPhotoPreviewFragment() {
        binding.layoutToolbar.visibility = View.GONE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, MenuProfilePhotoPreviewFragment.newInstance(), MenuProfilePhotoPreviewFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

     override fun showBottomSheetCommuteOptions(view: View?) {
        binding.layoutToolbar.visibility = View.GONE
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.root_fragment, BottomSheetCommuteOptions.newInstance(), BottomSheetCommuteOptions.TAG)
                .addToBackStack(null)
                .commit()
    }
     override fun showRouteSelectionFragment(view: View?) {
        binding.layoutToolbar.visibility = View.GONE
         val intent = Intent(this, RouteSelectionActivity::class.java)
         intent.putExtra("isFromAddressSelect", true)
         startActivity(intent)
    }

    override fun showDatePickerDialog(view: View?) {
        val dateTime = DateTime(DateTime.now())
        val mYear = dateTime.year
        val mMonth = dateTime.monthOfYear
        val mDay = dateTime.dayOfMonth
        val mHour = dateTime.hourOfDay
        val mMinute = dateTime.minuteOfHour
        createDatePickerDialog(view, mDay, mMonth, mYear, mHour, mMinute)
    }

    override fun showForgotPasswordFragment(view: View?) {
        binding.imageViewProfile.visibility = View.VISIBLE
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_fragment, ForgotPasswordFragment.newInstance(), ForgotPasswordFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    private fun createDatePickerDialog(view: View?, day: Int, month: Int, year: Int, hour: Int, minute: Int) {
        var month = month
        month--
        val mTimeSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, monthOfYear, dayOfMonth, 0, 0, 0)
            val dateTime = DateTime(cal.timeInMillis)
            when (view?.id) {
                R.id.text_view_driving_license_expire_date -> {
                    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val dateString = formatter.format(dateTime.toDate())
                    viewModel.drivingLicenceValidityDate.value = dateString
                }
                R.id.text_view_driving_license_given_date -> {
                    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val dateString = formatter.format(dateTime.toDate())
                    viewModel.drivingLicenceGivenDate.value = dateString
                }
            }
            datePickerDialog?.dismiss()
        }
        datePickerDialog = DatePickerDialog(this, R.style.DatePickerTheme, mTimeSetListener, year, month, day)
        datePickerDialog!!.setCancelable(true)
        datePickerDialog!!.show()
    }

}