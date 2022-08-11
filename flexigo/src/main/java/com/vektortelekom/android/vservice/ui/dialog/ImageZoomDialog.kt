package com.vektortelekom.android.vservice.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektor.ktx.data.remote.ApiHelper
import com.vektortelekom.android.vservice.databinding.DialogImageZoomBinding
import com.vektortelekom.android.vservice.utils.PhotoHelper

class ImageZoomDialog(context: Context, private val apiHelper: ApiHelper?, private val imageUrl: String, private val isFile: Boolean) : Dialog(context) {

    private lateinit var binding: DialogImageZoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DialogImageZoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (isFile)
            PhotoHelper.loadImageToImageView(context.applicationContext, imageUrl, binding.photoView)
        else
            PhotoHelper.loadImageToImageView(apiHelper!!, context.applicationContext, imageUrl, binding.photoView, false)

        binding.closeDialog.setOnClickListener {
            this.dismiss()
        }
    }
}