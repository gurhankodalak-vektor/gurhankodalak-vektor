package com.vektortelekom.android.vservice.ui.route.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.BottomSheetCalendarBinding
import com.vektortelekom.android.vservice.ui.route.search.RouteSearchViewModel
import com.vektortelekom.android.vservice.utils.convertToShuttleDate
import com.vektortelekom.android.vservice.utils.getDateWithZeroHour
import com.vektortelekom.android.vservice.utils.getDayWithoutHoursAndMinutesAsLong
import com.vektortelekom.android.vservice.utils.longToCalendar
import java.util.*
import javax.inject.Inject


class BottomSheetSingleDateCalendar : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetCalendarBinding

    private lateinit var viewModel: RouteSearchViewModel

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<BottomSheetCalendarBinding>(
            inflater,
            R.layout.bottom_sheet_calendar,
            container,
            false
        ).apply {
            lifecycleOwner = this@BottomSheetSingleDateCalendar
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startDate = longToCalendar(viewModel.currentWorkgroupResponse.value?.instance?.startDate) ?: Calendar.getInstance()

        if(viewModel.selectedTime.value == null){
            if (viewModel.currentWorkgroupResponse.value?.instance?.startDate!! > Calendar.getInstance().time.time) {
                viewModel.selectedTime.value = startDate.time.time
            } else
                viewModel.selectedTime.value = Calendar.getInstance().time.time
        }

        setMaximumDateCalendar()

        binding.calendarViewSelectDay.setOnDayClickListener { eventDay ->
            val clickedDayCalendar = eventDay.calendar.time.convertToShuttleDate()

            if (viewModel.mode.equals("startDay")) {
                if (eventDay.calendar.time.time >= viewModel.selectedTime.value!!)
                {
                    viewModel.selectedStartDayCalendar.value = eventDay.calendar.time
                    viewModel.selectedStartDay.value = clickedDayCalendar

                    if (viewModel.selectedFinishDayCalendar.value?.time!! < eventDay.calendar.time.time)
                        viewModel.selectedFinishDay.value = clickedDayCalendar

//                    viewModel.selectedTime.value = eventDay.calendar.time.time

                    dismiss()
                }

            } else if (viewModel.mode.equals("finishDay")) {

                if (eventDay.calendar.time.time <= (viewModel.selectedStartDayCalendar.value?.time!!.plus(30 * 1000L * 60 * 60 * 24))
                    && eventDay.calendar.time.time >= viewModel.selectedStartDayCalendar.value?.time!!) // 30 gün sonrasından büyük olmamalı
                {
                    viewModel.selectedFinishDayCalendar.value = eventDay.calendar.time
                    viewModel.selectedFinishDay.value = clickedDayCalendar

                    dismiss()
                }

            } else {
                viewModel.selectedCalendarDay.value =
                    eventDay.calendar.time.getDayWithoutHoursAndMinutesAsLong()
                        .getDateWithZeroHour()
                viewModel.dateValueText.value = clickedDayCalendar

                dismiss()
            }

        }


        binding.buttonContinue.visibility = View.GONE
    }
    private fun setMaximumDateCalendar(){

        if (viewModel.mode.equals("finishDay")) {
            val min = longToCalendar(viewModel.selectedStartDayCalendar.value?.time)
            val max = longToCalendar(viewModel.selectedStartDayCalendar.value?.time)
            max?.add(Calendar.MONTH, 1)

            binding.calendarViewSelectDay.setMinimumDate(min)
            binding.calendarViewSelectDay.setMaximumDate(longToCalendar(max?.time?.time))

        } else
        {
            val min = Calendar.getInstance()
            binding.calendarViewSelectDay.setMinimumDate(min)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = activity?.run { ViewModelProvider(requireActivity())[RouteSearchViewModel::class.java] }
                ?: throw Exception("Invalid Activity")

    }


}
