package com.vektortelekom.android.vservice.ui.shuttle.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.databinding.ShuttleStationChangeDialogBinding

class ShuttleStationChangeDialog(context: Context, private val stationName: String, val listener: ChangeSubmitListener?): Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val binding = ShuttleStationChangeDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textViewStationName.text = stationName

        binding.buttonSubmit.setOnClickListener {
            listener?.submit()
            dismiss()
        }

        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    interface ChangeSubmitListener{
        fun submit()
    }

}