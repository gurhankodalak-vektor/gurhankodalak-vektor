package com.vektortelekom.android.vservice.ui.calendar.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.CalendarAccountsDialogBinding
import com.vektortelekom.android.vservice.databinding.CalendarDialogBinding
import com.vektortelekom.android.vservice.ui.calendar.CalendarActivity
import com.vektortelekom.android.vservice.utils.convertForDay
import com.vektortelekom.android.vservice.utils.convertForMonth
import com.vektortelekom.android.vservice.utils.convertHoursAndMinutes
import java.util.*

class CalendarDialog(context: Context, val calendarItem: CalendarActivity.CalendarItem, val listener: CalendarDialogListener): Dialog(context) {
    private lateinit var binding: CalendarDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = CalendarDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textViewCalendarTitle.text = calendarItem.title

        val startDate = Date(calendarItem.startTime)

        binding.textViewDateDay.text = startDate.convertForDay()
        binding.textViewDateMonth.text = startDate.convertForMonth()
        binding.textViewDateFullDate.text = calendarItem.startMinutes.convertHoursAndMinutes().plus(" - ").plus(calendarItem.endMinutes.convertHoursAndMinutes())
        binding.textViewAddress.text = calendarItem.location

        binding.buttonSubmitPoolCar.setOnClickListener { listener.calendarItemClickedPoolCar(calendarItem, this) }

        binding.buttonSubmitFlexiride.setOnClickListener { listener.calendarItemClickedFlexiride(calendarItem, this) }

    }


    interface CalendarDialogListener {
        fun calendarItemClickedPoolCar(calendarItem: CalendarActivity.CalendarItem, dialog: Dialog)
        fun calendarItemClickedFlexiride(calendarItem: CalendarActivity.CalendarItem, dialog: Dialog)
    }

}