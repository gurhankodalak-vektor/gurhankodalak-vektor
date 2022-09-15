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
import java.util.*
import javax.inject.Inject


class BottomSheetSingleDateCalendar : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetCalendarBinding

    private lateinit var viewModel: RouteSearchViewModel
    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetCalendarBinding>(inflater, R.layout.bottom_sheet_calendar, container, false).apply {
            lifecycleOwner = this@BottomSheetSingleDateCalendar
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.calendarViewSelectDay.setOnDayClickListener { eventDay ->
            val clickedDayCalendar = eventDay.calendar.time.convertToShuttleDate()
            viewModel.selectedCalendarDay.value = eventDay.calendar.time.getDayWithoutHoursAndMinutesAsLong().getDateWithZeroHour()
            viewModel.dateValueText.value = clickedDayCalendar
            dismiss()
        }

        binding.buttonContinue.visibility = View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = activity?.run { ViewModelProvider(requireActivity())[RouteSearchViewModel::class.java] }
                ?: throw Exception("Invalid Activity")

    }


}
