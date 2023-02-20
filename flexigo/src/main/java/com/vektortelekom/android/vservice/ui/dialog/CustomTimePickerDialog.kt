package com.vektortelekom.android.vservice.ui.dialog

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Build
import android.text.format.DateFormat
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import java.util.*

class CustomTimePickerDialog(context: Context, listener: OnTimeSetListener,  private var hourOfDay: Int,  private var minuteOfHour: Int, is24HourView: Boolean, timePickerInterval: Int,
                             private var date1: Date?, private var date2: Date?,
                             themeResId: Int
) : TimePickerDialog(context, themeResId, listener, hourOfDay, minuteOfHour, is24HourView) {

    private val TIME_PICKER_INTERVAL = timePickerInterval
    private val mTimeSetListener: OnTimeSetListener = listener
    private var timePicker: TimePicker? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun updateTime(hourOfDay: Int, minuteOfHour: Int) {
        super.updateTime(hourOfDay, minuteOfHour)
        timePicker?.hour = hourOfDay
        timePicker?.minute = minuteOfHour / TIME_PICKER_INTERVAL
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

            val hourPicker = timePicker!!.findViewById<NumberPicker>(
                Resources.getSystem().getIdentifier(
                    "hour",
                    "id",
                    "android"
                )
            )

            minutePicker.maxValue = 60 / TIME_PICKER_INTERVAL - 1

            if ((date1 != null && date2 != null) && date1!!.compareTo(date2) == 0 && DateFormat.is24HourFormat(context)) {
                hourPicker.minValue = hourOfDay
                minutePicker.minValue = minuteOfHour

                setDisplayedValues(minutePicker, minuteOfHour)
            } else{
                minutePicker.minValue = 0
                setDisplayedValues(minutePicker, 0)
            }

            timePicker!!.setOnTimeChangedListener { _, pickerHour, _ ->
                if (hourOfDay == pickerHour) {
//                    minutePicker.minValue = minuteOfHour
                    setDisplayedValues(minutePicker, 0)
                } else{
                    if(minutePicker.displayedValues.contains("0")){
                        minutePicker.minValue = 0
                        setDisplayedValues(minutePicker, 0)
                    } else{
                        setDisplayedValues(minutePicker, 0)
                        minutePicker.minValue = 0
                    }
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDisplayedValues(minutePicker: NumberPicker, value : Int){
        val displayedValues: MutableList<String> = ArrayList()
        var i = value
        while (i < 60) {
            displayedValues.add(String.format("%02d", i))
            i += TIME_PICKER_INTERVAL
        }
        minutePicker.displayedValues = displayedValues.toTypedArray()
    }
}

