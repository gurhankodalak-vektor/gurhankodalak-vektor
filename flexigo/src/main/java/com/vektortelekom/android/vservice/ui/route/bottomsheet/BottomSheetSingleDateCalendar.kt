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
import com.vektortelekom.android.vservice.databinding.BottomSheetSingleDateCalendarBinding
import com.vektortelekom.android.vservice.ui.route.search.RouteSearchViewModel
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject


class BottomSheetSingleDateCalendar : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetSingleDateCalendarBinding

    private lateinit var viewModel: RouteSearchViewModel

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<BottomSheetSingleDateCalendarBinding>(
            inflater,
            R.layout.bottom_sheet_single_date_calendar,
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
        viewModel.selectedStartDayCalendar.value?.time

        setMaximumDateCalendar()

        binding.calendarViewSelectDay.setOnDayClickListener { eventDay ->
            val clickedDayCalendar = eventDay.calendar.time.convertToShuttleDate()

            if (viewModel.mode.equals("startDay")) {

                val date1: Date? = eventDay.calendar.time.convertForTimeCompare()
                val date2: Date? = longToCalendar(viewModel.selectedTime.value)?.time!!.convertForTimeCompare()

                if (date1?.compareTo(date2)!! > 0 || date1?.compareTo(date2) == 0){
                    viewModel.selectedStartDayCalendar.value = eventDay.calendar.time
                    viewModel.selectedStartDay.value = clickedDayCalendar
                    viewModel.selectedStartDay.value = viewModel.startDateFormatted(resources.configuration.locale.language)

                    if (viewModel.selectedFinishDayCalendar.value?.time!! < eventDay.calendar.time.time){
                        viewModel.selectedFinishDayCalendar.value = date1
                        viewModel.selectedFinishDay.value = clickedDayCalendar
                        viewModel.selectedFinishDay.value = viewModel.finishDateFormatted(resources.configuration.locale.language)
                    }

                    dismiss()
                }

            } else if (viewModel.mode.equals("finishDay")) {
                val date1: Date? = eventDay.calendar.time.convertForTimeCompare()
                val date2: Date? = longToCalendar(viewModel.selectedStartDayCalendar.value?.time!!)?.time.convertForTimeCompare()
                val date3: Date? = longToCalendar(viewModel.selectedStartDayCalendar.value?.time!!.plus(31 * 1000L * 60 * 60 * 24))?.time.convertForTimeCompare()

                if ((date1?.compareTo(date2) == 0 || date1?.compareTo(date2)!! > 0) && (date1.compareTo(date3) == 0 || date1.compareTo(date3) < 0))
                {
                    viewModel.selectedFinishDayCalendar.value = eventDay.calendar.time
                    viewModel.selectedFinishDay.value = clickedDayCalendar
                    viewModel.selectedFinishDay.value = viewModel.finishDateFormatted(resources.configuration.locale.language)

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
            min.add(Calendar.DATE, -1)
            binding.calendarViewSelectDay.setMinimumDate(min)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = activity?.run { ViewModelProvider(requireActivity())[RouteSearchViewModel::class.java] }
                ?: throw Exception("Invalid Activity")

    }


}
