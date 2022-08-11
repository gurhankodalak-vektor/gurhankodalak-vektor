package com.vektortelekom.android.vservice.ui.shuttle.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.ShuttleReservationDialogBinding
import com.vektortelekom.android.vservice.utils.convertForDay
import com.vektortelekom.android.vservice.utils.convertForMonth
import com.vektortelekom.android.vservice.utils.convertForShuttleDay
import java.util.*

class ShuttleReservationDialog(context: Context, private val stationName: String, private val date: Date, private val listener: ShuttleReservationListener): Dialog(context) {
    var isMorning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val  binding = ShuttleReservationDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textViewStationName.text = stationName

        binding.textViewDateFullDateStart.text = date.convertForShuttleDay()
        binding.textViewDateDayStart.text = date.convertForDay()
        binding.textViewDateMonthStart.text = date.convertForMonth()

        binding.cardViewReservationDay.setOnClickListener {
            listener.selectDate(this@ShuttleReservationDialog)
        }

        binding.textViewMorning.setOnClickListener {

            binding.textViewMorning.setBackgroundColor(ContextCompat.getColor(context, R.color.steel))
            binding.textViewMorning.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
            binding.textViewMorning.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
            binding.textViewMorning.setTextColor(ContextCompat.getColor(context, R.color.steel))

            isMorning = true

        }

        binding.textViewEvening.setOnClickListener {

            binding.textViewMorning.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
            binding.textViewMorning.setTextColor(ContextCompat.getColor(context, R.color.steel))
            binding.textViewMorning.setBackgroundColor(ContextCompat.getColor(context, R.color.steel))
            binding.textViewMorning.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))

            isMorning = false

        }

        binding.buttonSubmit.setOnClickListener {
            listener.makeReservation(isMorning)
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

    }

    interface ShuttleReservationListener {
        fun selectDate(dialog: ShuttleReservationDialog)
        fun makeReservation(isMorning: Boolean)

    }


}