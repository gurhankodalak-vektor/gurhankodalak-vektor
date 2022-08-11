package com.vektortelekom.android.vservice.ui.poolcar.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarOdometerDialogBinding
import java.lang.Exception

class OdometerDialog(context: Context, val listener: OdometerListener, val isStart: Boolean): Dialog(context) {

    private lateinit var binding: PoolCarOdometerDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = PoolCarOdometerDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if(isStart) {
            binding.textViewTitle.text = context.getString(R.string.odometer_title_start)
            binding.textViewText1.text = context.getString(R.string.odometer_text_start)
        }
        else {
            binding.textViewTitle.text = context.getString(R.string.odometer_title_end)
            binding.textViewText1.text = context.getString(R.string.odometer_text_end)
        }

        binding.buttonSubmitOdometer.setOnClickListener {
            try {
                listener.submitOdometer(binding.editTextKm.text.toString().toDouble())
                dismiss()
            }
            catch (e: Exception) {
                binding.textViewError.visibility = View.VISIBLE
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

    }

    interface OdometerListener{
        fun submitOdometer(value: Double)
    }

}