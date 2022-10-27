package com.vektortelekom.android.vservice.ui.dialog

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Build
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.annotation.RequiresApi


class CustomTimePickerDialog(context: Context, listener: OnTimeSetListener, hourOfDay: Int, minute: Int, is24HourView: Boolean,
                             themeResId: Int
) : TimePickerDialog(context, themeResId, listener, hourOfDay, minute, is24HourView) {

    private val TIME_PICKER_INTERVAL = 30
    private val mTimeSetListener: OnTimeSetListener = listener
    private var timePicker: TimePicker? = null


    override fun updateTime(hourOfDay: Int, minuteOfHour: Int) {
        super.updateTime(hourOfDay, minuteOfHour)
        timePicker?.currentHour = hourOfDay
        timePicker?.currentMinute = minuteOfHour / TIME_PICKER_INTERVAL
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(dialog: DialogInterface?, which: Int) {
        super.onClick(dialog, which)

        when(which){
            BUTTON_POSITIVE -> {
                if (mTimeSetListener != null && timePicker != null){
                    mTimeSetListener.onTimeSet(timePicker, timePicker!!.hour, timePicker!!.minute * TIME_PICKER_INTERVAL)
                }
            }
            BUTTON_NEGATIVE -> cancel()
        }
    }

    @SuppressLint("PrivateApi")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            timePicker = findViewById(
                Resources.getSystem().getIdentifier(
                    "timePicker",
                    "id",
                    "android"
                )
            )

            val minutePicker = timePicker!!.findViewById<NumberPicker>(
                Resources.getSystem().getIdentifier(
                    "minute",
                    "id",
                    "android"
                )
            )

            minutePicker.minValue = 0
            minutePicker.maxValue = 60 / TIME_PICKER_INTERVAL - 1
            val displayedValues: MutableList<String> = ArrayList()
            var i = 0
            while (i < 60) {
                displayedValues.add(String.format("%02d", i))
                i += TIME_PICKER_INTERVAL
            }
            minutePicker.displayedValues = displayedValues.toTypedArray()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

