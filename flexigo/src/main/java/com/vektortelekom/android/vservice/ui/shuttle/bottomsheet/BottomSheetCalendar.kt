package com.vektortelekom.android.vservice.ui.shuttle.bottomsheet

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.BottomSheetCalendarBinding
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.utils.convertForBackend2
import com.vektortelekom.android.vservice.utils.convertForDayAndMonth
import com.vektortelekom.android.vservice.utils.fromHtml
import java.util.*
import javax.inject.Inject


class BottomSheetCalendar : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetCalendarBinding

    private lateinit var viewModel: ShuttleViewModel
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private var selectedDays: List<Calendar> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<BottomSheetCalendarBinding>(inflater, R.layout.bottom_sheet_calendar, container, false).apply {
            lifecycleOwner = this@BottomSheetCalendar
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonContinue.setOnClickListener {
            selectedDays = binding.calendarViewSelectDay.selectedDates
            if (selectedDays.size > 1)
                showConfirmationMessage()
            else
                Toast.makeText(requireContext(), getString(R.string.selected_dates_warning), Toast.LENGTH_LONG).show()

        }
    }
    private fun showConfirmationMessage(){
        val dateRange = selectedDays.first().time.convertForDayAndMonth().plus(" - ")
                .plus(selectedDays.last().time.convertForDayAndMonth())

        var title = getString(R.string.confirm_absence)
        var message = getString(R.string.not_using_shuttle_message, viewModel.calendarSelectedRides.value!!.routeName, dateRange)


        if (viewModel.calendarSelectedRides.value?.notUsing == true) {
            title = getString(R.string.use_shuttle)
            message = getString(R.string.using_shuttle_message, viewModel.calendarSelectedRides.value!!.routeName, dateRange)
        }


        val dialog = AlertDialog.Builder(requireContext())
        dialog.setCancelable(true)
        dialog.setTitle(fromHtml("<b>$title</b>"))
        dialog.setMessage(message)
        dialog.setPositiveButton(resources.getString(R.string.confirm)) { d, _ ->
            d.dismiss()
            viewModel.calendarSelectedRides.value.let {
                if (it != null) {
                    viewModel.changeShuttleSelectedDate(it, selectedDays.first().time.convertForBackend2(), selectedDays.last().time.convertForBackend2(), true)
                   dismiss()

                }
            }

        }
        dialog.setNegativeButton(resources.getString(R.string.edit_dates)) { d, _ ->
            d.dismiss()
        }
        dialog.setNeutralButton(resources.getString(R.string.cancel)) { d, _ ->
            d.dismiss()
            dismiss()
        }

        dialog.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = activity?.run { ViewModelProvider(requireActivity())[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")

    }


}
