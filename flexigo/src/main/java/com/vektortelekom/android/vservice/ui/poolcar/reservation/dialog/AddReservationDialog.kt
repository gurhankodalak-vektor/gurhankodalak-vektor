package com.vektortelekom.android.vservice.ui.poolcar.reservation.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.databinding.PoolCarAddReservationDialogBinding

class AddReservationDialog(
        context: Context,
        private val park: String,
        private val to: String,
        private val purpose: String,
        private val startDate: String,
        private val endDate: String,
        private val description: String,
        private val listener: AddReservationListener
): Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val binding = PoolCarAddReservationDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textViewPark.text = park
        binding.textViewTo.text = to
        binding.textViewPurpose.text = purpose
        binding.textViewStart.text = startDate
        binding.textViewFinish.text = endDate
        binding.textViewDescription.text = description

        binding.buttonSubmit.setOnClickListener {
            dismiss()
            listener.addReservation()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

    }

    interface AddReservationListener {
        fun addReservation()
    }

}