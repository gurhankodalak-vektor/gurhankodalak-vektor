package com.vektortelekom.android.vservice.ui.carpool.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.CarpoolListItemBinding
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import kotlinx.android.extensions.LayoutContainer
import ru.rambler.libs.swipe_layout.SwipeLayout
import ru.rambler.libs.swipe_layout.SwipeLayout.OnSwipeListener


class CarPoolAdapter(var pageName: String, val listener: CarPoolAdapter.CarPoolSwipeListener): RecyclerView.Adapter<CarPoolAdapter.ViewHolder>() {

    private var list: List<CarPoolListModel> = listOf()
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
        holder.bind(list[position])
    }

    inner class ViewHolder (val binding: CarpoolListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(item: CarPoolListModel) {

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

            binding.swipeLayout.setOnSwipeListener(object : OnSwipeListener {
                override fun onBeginSwipe(swipeLayout: SwipeLayout, moveToRight: Boolean) {}
                override fun onSwipeClampReached(swipeLayout: SwipeLayout, moveToRight: Boolean) {

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

    fun setList(list: List<CarPoolListModel>) {
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