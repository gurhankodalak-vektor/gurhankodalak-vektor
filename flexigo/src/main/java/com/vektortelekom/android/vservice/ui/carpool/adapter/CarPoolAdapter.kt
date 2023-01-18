package com.vektortelekom.android.vservice.ui.carpool.adapter

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.databinding.CarpoolListItemBinding
import com.vektortelekom.android.vservice.utils.convertHourMinutes
import com.vektortelekom.android.vservice.utils.convertMetersToMile
import kotlinx.android.extensions.LayoutContainer
import ru.rambler.libs.swipe_layout.SwipeLayout
import ru.rambler.libs.swipe_layout.SwipeLayout.OnSwipeListener
import kotlin.collections.ArrayList

class CarPoolAdapter(val listener: CarPoolSwipeListener): RecyclerView.Adapter<CarPoolAdapter.ViewHolder>() {

    private var list: ArrayList<CarPoolListModel> = ArrayList()
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

        if (position == list.lastIndex){
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 300
            holder.itemView.layoutParams = params
        } else{
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 0
            holder.itemView.layoutParams = params
        }
    }

    inner class ViewHolder (val binding: CarpoolListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(item: CarPoolListModel, position: Int) {

            binding.swipeLayout.isSwipeEnabled = !isOnlyReadMode

            binding.imageviewCall.visibility = View.GONE
            binding.imageviewCancel.visibility = View.GONE
            binding.imageviewMatch.visibility = View.GONE
            binding.imageviewNavigation.visibility = View.GONE

            val time = (item.arrivalHour.convertHourMinutes(containerView.context) ?: "").plus(" - ").plus(item.departureHour.convertHourMinutes(containerView.context) ?: "")

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
                   // remove(position)
                    if (!moveToRight){
                        fadeOutAndHideImage(binding.imageViewDislike)
                        object: CountDownTimer(500, 500) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() {
                                remove(position)
                            }
                        }.start()

                        listener.onDislikeSwipe(item)
                    } else{
                        fadeOutAndHideImage(binding.imageViewLike)
                        object: CountDownTimer(500, 500) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() {
                                remove(position)
                            }
                        }.start()
                        listener.onLikeSwipe(item)
                    }

                }
                override fun onLeftStickyEdge(swipeLayout: SwipeLayout, moveToRight: Boolean) {}
                override fun onRightStickyEdge(swipeLayout: SwipeLayout, moveToRight: Boolean) {}
            })

        }
    }
    private fun fadeOutAndHideImage(img: ImageView) {
        val fadeOut = AlphaAnimation(1F, 0F)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = 500

        fadeOut.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationEnd(animation:Animation) {
                img.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation:Animation) {}
            override  fun onAnimationStart(animation:Animation) {}
        })
        img.startAnimation(fadeOut)
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

    fun isOnlyReadMode(isOnlyReadMode: Boolean) {
        this.isOnlyReadMode = isOnlyReadMode
        notifyDataSetChanged()
    }

}