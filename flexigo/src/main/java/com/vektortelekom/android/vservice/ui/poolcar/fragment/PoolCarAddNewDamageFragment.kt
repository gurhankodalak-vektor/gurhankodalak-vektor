package com.vektortelekom.android.vservice.ui.poolcar.fragment

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.vektor.ktx.utils.ImageHelper
import com.vektor.ktx.utils.PermissionsUtils
import com.vektor.vshare_api_ktx.model.DamageModel
import com.vektor.vshare_api_ktx.model.DamageRegion
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarAddNewDamageFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarViewModel
import com.vektortelekom.android.vservice.ui.poolcar.adapter.AddDamagePhotosAdapter
import com.vektortelekom.android.vservice.utils.GlideApp
import com.vektortelekom.android.vservice.utils.convertBackendDateToLong
import com.vektortelekom.android.vservice.utils.convertMillisecondsToMinutesSeconds
import com.vektortelekom.android.vservice.utils.dpToPx
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class PoolCarAddNewDamageFragment: BaseFragment<PoolCarViewModel>(), PermissionsUtils.CameraStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: PoolCarViewModel

    private lateinit var binding: PoolCarAddNewDamageFragmentBinding

    private var createRentalTime: Long?= null

    var timer: Timer? = null

    var handler: Handler? = null

    lateinit var damage: DamageModel

    private var mPhotoFile = ""

    private val PICK_IMAGE_CAMERA = 2009

    private var candidateRegion : DamageRegion? = null

    lateinit var adapter: AddDamagePhotosAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<PoolCarAddNewDamageFragmentBinding>(inflater, R.layout.pool_car_add_new_damage_fragment, container, false).apply {
            lifecycleOwner = this@PoolCarAddNewDamageFragment
            viewModel = this@PoolCarAddNewDamageFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        viewModel.rental.observe(viewLifecycleOwner) { rental ->

            createRentalTime = rental.creationTime.convertBackendDateToLong()

            timer?.cancel()

            timer = Timer()

            timer?.schedule(getTimerTask(), 0, 1000)
        }

        damage = DamageModel()
        damage.fileUuids = mutableListOf()

        adapter = AddDamagePhotosAdapter(damage, object: AddDamagePhotosAdapter.DamageListener {
            override fun addDamage() {
                if(requireActivity() is BaseActivity<*> && (requireActivity() as BaseActivity<*>).checkAndRequestCameraPermission(this@PoolCarAddNewDamageFragment)) {
                    onCameraPermissionOk()
                }
            }
        })

        binding.recyclerViewPhotos.adapter = adapter

        binding.recyclerViewPhotos.addItemDecoration(object: RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)

                with(outRect) {
                    left = if (parent.getChildAdapterPosition(view) == 0) {
                        20f.dpToPx(requireContext())
                    } else {
                        5f.dpToPx(requireContext())
                    }
                    right = if (parent.getChildAdapterPosition(view) == damage.fileUuids?.size) {
                        20f.dpToPx(requireContext())
                    } else {
                        5f.dpToPx(requireContext())
                    }
                }
            }
        })

        binding.buttonSendDamage.setOnClickListener {
            val description = binding.editTextDescription.text.toString()

            if(description.isEmpty()) {
                FlexigoInfoDialog.Builder(requireContext())
                        .setIconVisibility(false)
                        .setTitle(getString(R.string.description))
                        .setText1(getString(R.string.description_empty))
                        .setOkButton(getString(R.string.Generic_Ok)) { dialog1 ->
                            dialog1.dismiss()

                        }
                        .create().show()
            }
            else {
                damage.description = description
                viewModel.addCarDamage(damage, true)
            }

        }

        binding.cardViewAddDamageFront.setOnClickListener {

            selectDamageClicked(DamageRegion.FRONT)

            binding.textViewReportExternalDamage.text = getString(R.string.report_front_external_damage)

        }

        binding.cardViewAddDamageRear.setOnClickListener {

            selectDamageClicked(DamageRegion.REAR)

            binding.textViewReportExternalDamage.text = getString(R.string.report_rear_external_damage)

        }

        binding.cardViewAddDamageRight.setOnClickListener {

            selectDamageClicked(DamageRegion.RIGHT)

            binding.textViewReportExternalDamage.text = getString(R.string.report_right_external_damage)

        }

        binding.cardViewAddDamageLeft.setOnClickListener {

            selectDamageClicked(DamageRegion.LEFT)

            binding.textViewReportExternalDamage.text = getString(R.string.report_left_external_damage)

        }

        viewModel.isDamageAdded.observe(viewLifecycleOwner) { result ->

            if (result != null) {

                damage.region2?.let {
                    setRegionUnselected(it)
                }

                damage = DamageModel()
                damage.fileUuids = mutableListOf()

                damage.region2 = candidateRegion

                damage.region2?.let { setRegionSelected(it) }

                requireActivity().currentFocus?.clearFocus()

                binding.layoutAddDamage.requestFocus()
                adapter.changeDamage(damage)
                binding.editTextDescription.setText("")

                viewModel.isDamageAdded.value = null
            }

        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        timer?.cancel()

        timer = Timer()

        timer?.schedule(getTimerTask(), 0, 1000)

    }

    override fun onDetach() {
        timer?.cancel()
        super.onDetach()
    }

    private fun selectDamageClicked(region: DamageRegion) {
        binding.layoutAddDamage.visibility = View.VISIBLE

        damage.region2?.let { region2 ->
            if(damage.fileUuids.isNullOrEmpty()) {
                setRegionUnselected(region2)
                damage.region2 = region

                damage.region2?.let { setRegionSelected(it) }

                requireActivity().currentFocus?.clearFocus()

                binding.layoutAddDamage.requestFocus()

                return@let
            }
            else {
                FlexigoInfoDialog.Builder(requireContext())
                        .setIconVisibility(false)
                        .setTitle(getString(R.string.damage_dialog_title))
                        .setText1(getString(R.string.damage_dialog_text1))
                        .setOkButton(getString(R.string.submit)) { dialog ->
                            dialog.dismiss()

                            val description = binding.editTextDescription.text.toString()

                            if(description.isEmpty()) {

                                FlexigoInfoDialog.Builder(requireContext())
                                        .setIconVisibility(false)
                                        .setTitle(getString(R.string.description))
                                        .setText1(getString(R.string.description_empty))
                                        .setOkButton(getString(R.string.Generic_Ok)) { dialog1 ->
                                            dialog1.dismiss()

                                        }
                                        .create().show()

                            }
                            else {
                                candidateRegion = region
                                damage.description = description
                                viewModel.addCarDamage(damage, false)
                            }
                        }
                        .setCancelButton(getString(R.string.cancel)) { dialog ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
            }
        }
    }

    private fun setRegionUnselected(region: DamageRegion) {
        when(region) {
            DamageRegion.FRONT -> {
                binding.cardViewAddDamageFront.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                binding.imageViewFront.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purpley))
            }
            DamageRegion.REAR -> {
                binding.cardViewAddDamageRear.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                binding.imageViewRear.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purpley))
            }
            DamageRegion.RIGHT -> {
                binding.cardViewAddDamageRight.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                binding.imageViewRight.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purpley))
            }
            DamageRegion.LEFT -> {
                binding.cardViewAddDamageLeft.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                binding.imageViewLeft.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purpley))
            }
            else -> {

            }
        }
    }

    private fun setRegionSelected(region: DamageRegion) {
        when(region) {
            DamageRegion.FRONT -> {
                binding.cardViewAddDamageFront.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purpley))
                binding.imageViewFront.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            }
            DamageRegion.REAR -> {
                binding.cardViewAddDamageRear.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purpley))
                binding.imageViewRear.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            }
            DamageRegion.RIGHT -> {
                binding.cardViewAddDamageRight.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purpley))
                binding.imageViewRight.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            }
            DamageRegion.LEFT -> {
                binding.cardViewAddDamageLeft.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purpley))
                binding.imageViewLeft.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            }
            else -> {

            }
        }
    }

    private fun getTimerTask(): TimerTask {
        return object: TimerTask() {
            override fun run() {
                if(createRentalTime == null) {
                    return
                }

                val timeDiff = createRentalTime!! + 1000*60*15 - System.currentTimeMillis()

                if(timeDiff > 0) {
                    handler?.post {
                        binding.textViewRemainingTime.text = timeDiff.convertMillisecondsToMinutesSeconds()
                    }
                }
                else {
                    handler?.post {
                        binding.textViewRemainingTime.text = getString(R.string.rental_started)
                        timer?.cancel()
                    }
                }
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

    override fun onCameraPermissionFailed() {

    }

    private fun setPhotoUri(): Uri {
        val result = ImageHelper.getPhotoFile(requireContext())
        this.mPhotoFile = result.photoFile
        return result.photoUri!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PICK_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {

            if(requireActivity() is BaseActivity<*>) {
                val activity = requireActivity() as BaseActivity<*>

                activity.showPd()

                GlideApp.with(activity).asBitmap()
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
                                    viewModel.addNewDamagePreviewPhotoPath = mPhotoFile
                                    viewModel.navigator?.showPoolCarAddNewDamagePreviewFragment(null)
                                    activity.dismissPd()
                                }

                                /*activity.runOnUiThread {
                                    damage.fileUuids?.add(mPhotoFile)
                                    binding.recyclerViewPhotos.adapter?.notifyDataSetChanged()
                                    activity.dismissPd()
                                }*/
                                return true
                            }
                        }).submit()

            }
        }
    }

    fun addNewPhoto() {
        damage.fileUuids?.add(mPhotoFile)
        binding.recyclerViewPhotos.adapter?.notifyDataSetChanged()
    }

    fun takePhotoAgain() {
        onCameraPermissionOk()
    }

    override fun getViewModel(): PoolCarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[PoolCarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "PoolCarAddNewDamageFragment"

        fun newInstance() = PoolCarAddNewDamageFragment()

    }

}