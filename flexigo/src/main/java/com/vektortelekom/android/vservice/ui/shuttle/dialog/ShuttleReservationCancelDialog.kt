package com.vektortelekom.android.vservice.ui.shuttle.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.data.model.RouteModel
import com.vektortelekom.android.vservice.data.model.ShuttleDayModel
import com.vektortelekom.android.vservice.databinding.ShuttleReservationCancelDialogBinding
import com.vektortelekom.android.vservice.utils.convertBackendDateToShuttleReservationString

class ShuttleReservationCancelDialog(context: Context, private val shuttleDay: ShuttleDayModel, private val routeDetail: RouteModel, private val listener: ShuttleReservationCancelListener): Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val binding = ShuttleReservationCancelDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textViewDriver.text = routeDetail.driver.name.plus(" ").plus(routeDetail.driver.surname)
        binding.textViewPlate.text = routeDetail.vehicle.plateId
        binding.textViewRoute.text = routeDetail.name
        binding.textViewDate.text = shuttleDay.shuttleDay.convertBackendDateToShuttleReservationString()
        binding.textViewShift.text = routeDetail.shift.name

        binding.cardViewDriver.setOnClickListener {
            routeDetail.driver.phoneNumber.let {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:".plus(it)))
                context.startActivity(intent)
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSubmit.setOnClickListener {
            listener.cancelReservation()
        }

    }

    interface ShuttleReservationCancelListener {
        fun cancelReservation()

    }

}