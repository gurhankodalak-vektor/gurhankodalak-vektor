package com.vektortelekom.android.vservice.ui.carpool.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.CarpoolListItemBinding
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import com.vektortelekom.android.vservice.utils.convertMetersToMile
import kotlinx.android.extensions.LayoutContainer
import ru.rambler.libs.swipe_layout.SwipeLayout
import ru.rambler.libs.swipe_layout.SwipeLayout.OnSwipeListener


class CarPoolAdapter(var pageName: String, val listener: CarPoolSwipeListener): RecyclerView.Adapter<CarPoolAdapter.ViewHolder>() {

    private var list: ArrayList<CarPoolListModel> = ArrayList()
    private var isDriver: Boolean = false
    private var isRider: Boolean = false
    private var isOnlyReadMode: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarPoolAdapter.ViewHolder {
        val binding = CarpoolListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    inner class ViewHolder (val binding: CarpoolListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(item: CarPoolListModel, position: Int) {
            val position = position

            if (pageName == "drivers")
                binding.swipeLayout.isSwipeEnabled = !isDriver

            if (pageName == "riders")
                binding.swipeLayout.isSwipeEnabled = !isRider

            if (isOnlyReadMode)
                binding.swipeLayout.isSwipeEnabled = false

            binding.imageviewCall.visibility = View.GONE
            binding.imageviewCancel.visibility = View.GONE
            binding.imageviewMatch.visibility = View.GONE
            binding.imageviewNavigation.visibility = View.GONE

            val time = item.arrivalHour.convertHourMinutes() ?: "".plus(" - ").plus(item.departureHour.convertHourMinutes() ?: "")

            binding.textviewNameSurname.text = item.name.plus(" ").plus(item.surname)
            binding.textviewDepartment.text = item.department ?: ""
            binding.textviewDepartureTime.text = time

            val number2digits = String.format("%.2f", item.durationInMin).plus(" min")
            val meterToMile = String.format("%.2f", item.distanceInMeter?.convertMetersToMile()).plus(" mi")
            val meterAndTime = meterToMile.plus(", +").plus(number2digits)

            binding.textviewTime.text = meterAndTime

            binding.swipeLayout.setOnSwipeListener(object : OnSwipeListener {
                override fun onBeginSwipe(swipeLayout: SwipeLayout, moveToRight: Boolean) {}
                override fun onSwipeClampReached(swipeLayout: SwipeLayout, moveToRight: Boolean) {
                    remove(position)

                    if (!moveToRight)
                        listener.onDislikeSwipe(item)
                    else
                        listener.onLikeSwipe(item)

                }
                override fun onLeftStickyEdge(swipeLayout: SwipeLayout, moveToRight: Boolean) {}
                override fun onRightStickyEdge(swipeLayout: SwipeLayout, moveToRight: Boolean) {}
            })

        }
    }


    interface CarPoolSwipeListener {
        fun onDislikeSwipe(item: CarPoolListModel)
        fun onLikeSwipe(item: CarPoolListModel)
    }
    fun remove(position: Int) {
        if (list.size > 0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setList(list: ArrayList<CarPoolListModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun setIsDriver(isDriver: Boolean) {
        this.isDriver = isDriver
        notifyDataSetChanged()
    }

    fun setIsRider(isRider: Boolean) {
        this.isRider = isRider
        notifyDataSetChanged()
    }

    fun isOnlyReadMode(isOnlyReadMode: Boolean) {
        this.isOnlyReadMode = isOnlyReadMode
        notifyDataSetChanged()
    }

}