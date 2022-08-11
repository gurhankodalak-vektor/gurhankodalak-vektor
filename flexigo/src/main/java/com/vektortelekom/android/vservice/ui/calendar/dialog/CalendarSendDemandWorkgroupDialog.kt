package com.vektortelekom.android.vservice.ui.calendar.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.data.model.workgroup.WorkGroupInstance
import com.vektortelekom.android.vservice.databinding.CalendarDemandRequestWorkgroupDialogBinding

class CalendarSendDemandWorkgroupDialog(context: Context, val workgroupInstance : WorkGroupInstance, val text: String, val listener: CalendarDialogListener) : Dialog(context) {

    private lateinit var binding: CalendarDemandRequestWorkgroupDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = CalendarDemandRequestWorkgroupDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textViewWorkgroupRequestDemandInfo.text = text

        binding.buttonSendDemandRequest.setOnClickListener { listener.sendDemandRequestWorkgroup(workgroupInstance, this) }

        binding.buttonSubmitFlexiride.setOnClickListener { dismiss() }

    }


    interface CalendarDialogListener {
        fun sendDemandRequestWorkgroup(workgroupInstance : WorkGroupInstance, dialog: Dialog)
    }

}