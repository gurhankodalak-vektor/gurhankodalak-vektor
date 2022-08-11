package com.vektortelekom.android.vservice.ui.menu.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.ui.base.photo.TakePhotoActivity
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.MenuDrivingLicenseFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.dialog.ImageZoomDialog
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import com.vektortelekom.android.vservice.ui.menu.MenuViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class MenuDrivingLicenseFragment : BaseFragment<MenuViewModel>(), PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    lateinit var binding: MenuDrivingLicenseFragmentBinding

    private val PICK_IMAGE_CAMERA_FRONT = 1
    private val PICK_IMAGE_CAMERA_REAR = 2
    private var isFront = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<MenuDrivingLicenseFragmentBinding>(inflater, R.layout.menu_driving_license_fragment, container, false).apply {
            lifecycleOwner = this@MenuDrivingLicenseFragment
            viewModel = this@MenuDrivingLicenseFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageViewDriverFront.setOnClickListener {
            val imageZoomDialog = ImageZoomDialog(requireContext(), AppApiHelper(), viewModel.mPhotoFileFront, true)
            imageZoomDialog.show()
        }

        binding.deleteFrontPhoto.setOnClickListener {
            binding.cardViewAddFront.visibility = View.VISIBLE
            binding.cardViewShowFront.visibility = View.GONE
            binding.imageViewDriverFront.setImageBitmap(null)
            viewModel.mPhotoFileFront = ""
        }

        binding.imageViewDriverRear.setOnClickListener {
            val imageZoomDialog = ImageZoomDialog(requireContext(), AppApiHelper(), viewModel.mPhotoFileRear, true)
            imageZoomDialog.show()
        }

        binding.deleteRearPhoto.setOnClickListener {
            binding.cardViewAddRear.visibility = View.VISIBLE
            binding.cardViewShowRear.visibility = View.GONE
            binding.imageViewDriverRear.setImageBitmap(null)
            viewModel.mPhotoFileRear = ""
        }

        binding.cardViewAddFront.setOnClickListener {
            isFront = true
            if ((requireActivity() as MenuActivity).checkAndRequestCameraPermission(this).not()) {
                return@setOnClickListener
            }
            onCameraPermissionOk()
        }

        binding.cardViewAddRear.setOnClickListener {
            isFront = false
            if ((requireActivity() as MenuActivity).checkAndRequestCameraPermission(this).not()) {
                return@setOnClickListener
            }
            onCameraPermissionOk()
        }

        viewModel.addDocumentResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                FlexigoInfoDialog.Builder(requireContext())
                        .setIconVisibility(false)
                        .setTitle(getString(R.string.driving_license_added_success_title))
                        .setText1(getString(R.string.driving_license_added_success_text))
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog1 ->
                            dialog1.dismiss()
                            activity?.setResult(Activity.RESULT_OK)
                            activity?.finish()

                        }
                        .create().show()
                viewModel.addDocumentResponse.value = null
            }
        }

    }

    override fun onCameraPermissionOk() {
        startTakePhotoActivity()
    }

    private fun startTakePhotoActivity(photoFile: String? = null) {
        val intent = Intent(requireContext(), TakePhotoActivity::class.java)
        if (photoFile != null) {
            intent.putExtra("photoFile", photoFile)
        }
        if (isFront) {
            intent.putExtra("description", "Ehliyetinizin ön yüzünün net bir fotoğrafını ekleyiniz.")
            startActivityForResult(intent, PICK_IMAGE_CAMERA_FRONT)
        } else {
            intent.putExtra("description", "Ehliyetinizin arka yüzünün net bir fotoğrafını ekleyiniz.")
            startActivityForResult(intent, PICK_IMAGE_CAMERA_REAR)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == DaggerAppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE_CAMERA_FRONT) {
            if (data != null) {
                val photoFile = data.getStringExtra("photoFile")
                if (photoFile != null) {
                    viewModel.mPhotoFileFront = photoFile
                    processImage(photoFile, true)
                }
            }
            /*card_view_add_front.visibility = View.GONE
            card_view_show_front.visibility = View.VISIBLE
            image_view_driver_front.setImageURI(Uri.parse(viewModel.mPhotoFileFront))*/
        } else if (resultCode == DaggerAppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE_CAMERA_REAR) {
            if (data != null) {
                val photoFile = data.getStringExtra("photoFile")
                if (photoFile != null) {
                    viewModel.mPhotoFileRear = photoFile
                    processImage(photoFile, false)
                }
            }
        }
    }

    private fun processImage(uri: String, isFront: Boolean) {
        requireActivity().runOnUiThread {
            if (isFront) {
                binding.cardViewAddFront.visibility = View.GONE
                binding.cardViewShowFront.visibility = View.VISIBLE
                binding.imageViewDriverFront.setImageURI(Uri.parse(uri))
            } else {
                binding.cardViewAddRear.visibility = View.GONE
                binding.cardViewShowRear.visibility = View.VISIBLE
                binding.imageViewDriverRear.setImageURI(Uri.parse(uri))
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionsUtils.onRequestPermissionsResult(requestCode, grantResults, this)
    }

    override fun onCameraPermissionFailed() {

    }

    override fun getViewModel(): MenuViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[MenuViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "MenuDrivingLicenseFragment"

        fun newInstance() = MenuDrivingLicenseFragment()

    }

}