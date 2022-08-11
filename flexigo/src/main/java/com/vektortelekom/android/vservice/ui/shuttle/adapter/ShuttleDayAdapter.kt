package com.vektortelekom.android.vservice.ui.shuttle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.ShuttleDayModel
import com.vektortelekom.android.vservice.databinding.ShuttleInformationViewHolderItemBinding
import com.vektortelekom.android.vservice.ui.dialog.FlexigoInfoDialog
import com.vektortelekom.android.vservice.utils.convertBackendDateToLong
import com.vektortelekom.android.vservice.utils.convertToShuttleDayItem
import kotlinx.android.extensions.LayoutContainer
import java.util.*

class ShuttleDayAdapter(private var shuttleDays: List<ShuttleDayModel>, val listener: ShuttleItemListener?): RecyclerView.Adapter<ShuttleDayAdapter.ShuttleDayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuttleDayViewHolder {
        val binding = ShuttleInformationViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ShuttleDayViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return shuttleDays.size
    }

    override fun onBindViewHolder(holder: ShuttleDayViewHolder, position: Int) {
        holder.bind(shuttleDays[position])
    }

    inner class ShuttleDayViewHolder (val binding: ShuttleInformationViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(model: ShuttleDayModel) {

            val dayText = Date(model.shuttleDay.convertBackendDateToLong()?:0L).convertToShuttleDayItem()

            binding.textViewDay.text = dayText

            if(model.isIncoming == true) {
                binding.checkBoxMorning.isChecked = true
                binding.checkBoxMorning.buttonDrawable = ContextCompat.getDrawable(containerView.context, R.drawable.ic_circular_check_box)
            }
            else {
                binding.checkBoxMorning.isChecked = false
                if(model.bookedIncomingRoute?.id == null) {
                    binding.checkBoxMorning.buttonDrawable = ContextCompat.getDrawable(containerView.context, R.drawable.ic_circular_check_box)
                }
                else {
                    binding.checkBoxMorning.buttonDrawable = ContextCompat.getDrawable(containerView.context, R.drawable.ic_circular_check_box_reservation)
                }
            }

            if(model.isOutgoing == true) {
                binding.checkBoxEvening.isChecked = true
                binding.checkBoxEvening.buttonDrawable = ContextCompat.getDrawable(containerView.context, R.drawable.ic_circular_check_box)
            }
            else {
                binding.checkBoxEvening.isChecked = false
                if(model.bookedOutgoingRoute?.id == null) {
                    binding.checkBoxEvening.buttonDrawable = ContextCompat.getDrawable(containerView.context, R.drawable.ic_circular_check_box)
                }
                else {
                    binding.checkBoxEvening.buttonDrawable = ContextCompat.getDrawable(containerView.context, R.drawable.ic_circular_check_box_reservation)
                }
            }


            binding.checkBoxMorning.setOnCheckedChangeListener { buttonView, isChecked ->

                //This check is necessary See below link:
                //https://stackoverflow.com/questions/27641705/oncheckedchanged-called-automatically
                if(buttonView.isPressed) {

                    if(model.bookedIncomingRoute?.id == null) {

                        val context = containerView.context

                        FlexigoInfoDialog.Builder(context)
                                .setIconVisibility(false)
                                .setCancelable(false)
                                .setTitle(context.getString(R.string.shuttle_use_change_question_title))
                                .setText1(context.getString(R.string.shuttle_use_change_question,
                                        context.getString(if(isChecked) R.string.shuttle_use_change_question_true else R.string.shuttle_use_change_question_false),
                                        dayText.plus(" ").plus(context.getString(R.string.morning))))
                                .setOkButton(context.getString(R.string.Generic_Ok)) {
                                    it.dismiss()
                                    model.isIncoming = isChecked
                                    listener?.shuttleItemClicked(model)
                                }
                                .setCancelButton(context.getString(R.string.cancel)) {
                                    binding.checkBoxMorning.isChecked = isChecked.not()
                                    it.dismiss()
                                }
                                .create()
                                .show()
                    }
                    else {
                        listener?.shuttleMorningReservationClicked(model)
                    }

                }

            }

            binding.checkBoxEvening.setOnCheckedChangeListener { buttonView, isChecked ->

                if(buttonView.isPressed) {
                    if(model.bookedOutgoingRoute?.id == null) {
                        val context = containerView.context

                        FlexigoInfoDialog.Builder(context)
                                .setIconVisibility(false)
                                .setCancelable(false)
                                .setTitle(context.getString(R.string.shuttle_use_change_question_title))
                                .setText1(context.getString(R.string.shuttle_use_change_question,
                                        context.getString(if(isChecked) R.string.shuttle_use_change_question_true else R.string.shuttle_use_change_question_false),
                                        dayText.plus(" ").plus(context.getString(R.string.evening))))
                                .setOkButton(context.getString(R.string.Generic_Ok)) {
                                    it.dismiss()
                                    model.isOutgoing = isChecked
                                    listener?.shuttleItemClicked(model)
                                }
                                .setCancelButton(context.getString(R.string.cancel)) {
                                    binding.checkBoxEvening.isChecked = isChecked.not()
                                    it.dismiss()
                                }
                                .create()
                                .show()
                    }
                    else {
                        listener?.shuttleEveningReservationClicked(model)
                    }

                }

            }

            /*check_box_overtime.setOnCheckedChangeListener { buttonView, isChecked ->

                if(buttonView.isPressed) {
                    model.isMesai = isChecked
                    listener?.shuttleOvertimeItemClicked(model)

                }

            }*/

        }

    }

    fun setList(shuttleDays: List<ShuttleDayModel>) {
        //isDialogShown = false
        this.shuttleDays = shuttleDays
        notifyDataSetChanged()
    }

    interface ShuttleItemListener {
        fun shuttleItemClicked(model: ShuttleDayModel)
        fun shuttleMorningReservationClicked(model: ShuttleDayModel)
        fun shuttleEveningReservationClicked(model: ShuttleDayModel)
        fun shuttleOvertimeItemClicked(model: ShuttleDayModel)
    }

}