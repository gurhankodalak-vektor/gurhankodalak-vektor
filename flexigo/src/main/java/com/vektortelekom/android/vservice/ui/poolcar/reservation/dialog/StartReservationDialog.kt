package com.vektortelekom.android.vservice.ui.poolcar.reservation.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.PoolcarAndFlexirideModel
import com.vektortelekom.android.vservice.databinding.PoolCarReservationStartReservationDialogBinding

class StartReservationDialog(context: Context, val reservation: PoolcarAndFlexirideModel, private val startClicked: () -> Unit, private val qrClicked: () -> Unit): Dialog(context) {

    private lateinit var binding: PoolCarReservationStartReservationDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.pool_car_reservation_start_reservation_dialog, null, false)
        setContentView(binding.root)

        window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textViewPlate.text = reservation.vehicle?.plate


        binding.buttonStartRental.setOnClickListener {
            dismiss()
            startClicked()
        }

        binding.buttonReadQr.setOnClickListener {
            dismiss()
            qrClicked()
        }

    }

}