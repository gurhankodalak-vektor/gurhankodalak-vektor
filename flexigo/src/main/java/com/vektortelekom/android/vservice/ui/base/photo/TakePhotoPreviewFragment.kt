package com.vektortelekom.android.vservice.ui.base.photo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektor.ktx.utils.ImageHelper
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.TakePhotoPreviewFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import javax.inject.Inject

class TakePhotoPreviewFragment: BaseFragment<TakePhotoViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: TakePhotoViewModel
    private lateinit var binding: TakePhotoPreviewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate<TakePhotoPreviewFragmentBinding>(inflater, R.layout.take_photo_preview_fragment, container, false).apply {
            lifecycleOwner = this@TakePhotoPreviewFragment
            viewModel = this@TakePhotoPreviewFragment.viewModel
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setImageBitmap(viewModel.bitmapPhoto)

        binding.done.setOnClickListener {

            if(viewModel.mPhotoFile == null) {
                setPhotoUri()
            }

            AppDataManager.instance.saveBitmapAsJpeg(viewModel.bitmapPhoto!!, viewModel.mPhotoFile!!)

            val resultIntent = Intent()
            resultIntent.putExtra("photoFile", viewModel.mPhotoFile)
            requireActivity().setResult(Activity.RESULT_OK, resultIntent)
            requireActivity().finish()

        }

    }

    private fun setPhotoUri(): Uri {
        val result = ImageHelper.getPhotoFile(requireContext())
        viewModel.mPhotoFile = result.photoFile

        return result.photoUri!!
    }


    override fun getViewModel(): TakePhotoViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[TakePhotoViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "TakePhotoPreviewFragment"

        fun newInstance(): TakePhotoPreviewFragment {
            return TakePhotoPreviewFragment()
        }
    }

}