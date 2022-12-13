package com.vektortelekom.android.vservice.ui.home.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.vektor.ktx.ui.binding.getParentActivity
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.DashboardItemType
import com.vektortelekom.android.vservice.data.model.DashboardModel
import com.vektortelekom.android.vservice.databinding.HomeDashboardViewHolderItemBinding
import com.vektortelekom.android.vservice.ui.base.HighlightView
import com.vektortelekom.android.vservice.utils.fromCamelCaseToSnakeCase
import kotlinx.android.extensions.LayoutContainer

class DashboardAdapter(private val dashboard: List<DashboardModel>, val listener: DashboardItemListener?, val nestedScrollView: NestedScrollView, var countPoolCarVehicle: Int?): RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder>() {

    var commentsView : View? = null
    var commentsViewPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val binding = HomeDashboardViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return DashboardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dashboard.size
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        holder.bind(dashboard[position], position)
    }

    inner class DashboardViewHolder ( val binding: HomeDashboardViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(model: DashboardModel, position: Int) {
            binding.imageViewIcon.setImageResource(containerView.context.resources.getIdentifier("ic_dashboard_${model.iconName.fromCamelCaseToSnakeCase()}", "drawable", containerView.context.packageName))

            binding.textViewTitle.text = model.title

            binding.textViewDesc.visibility = if(model.subTitle == null) View.INVISIBLE else View.VISIBLE

            binding.textViewDesc.text = model.subTitle

            binding.cardViewInfo.visibility = if(model.info == null) View.INVISIBLE else View.VISIBLE

            if(model.type == DashboardItemType.PoolCar && countPoolCarVehicle != null) {
                binding.textViewInfo.text = containerView.context.getString(R.string.available_vehicle, countPoolCarVehicle.toString())
            }
            else {
                binding.textViewInfo.text = model.info
            }
            if(BuildConfig.FLAVOR != "tums") {
                try{
                    binding.cardViewRoot.setCardBackgroundColor(Color.parseColor("#${model.tintColor}"))
                }
                catch (e: Exception) {
                    binding.cardViewRoot.setCardBackgroundColor(ContextCompat.getColor(containerView.context, R.color.colorPrimary))
                }
            }


            binding.cardViewRoot.setOnClickListener {
                listener?.itemClicked(model)
            }

//            if(model.type == DashboardItemType.Shuttle) {
//
//                containerView.postDelayed({
//
//                    containerView.getParentActivity()?.let { activity ->
//
//                        HighlightView.Builder(containerView.context, binding.cardViewRoot, activity,"home_shuttle", "sequence_home_activity")
//                                .setHighlightText(containerView.context.getString(R.string.tutorial_shuttle))
//                                .addGotItListener {
//
//                                    containerView.post {
//                                        commentsView?.let { commentsView ->
//
//                                            HighlightView.Builder(containerView.context, commentsView, activity, "home_comments", "sequence_home_activity")
//                                                    .setHighlightText(containerView.context.getString(R.string.tutorial_comments))
//                                                    .addGotItListener {
//                                                        listener?.highlightCompleted()
//                                                    }
//                                                    .addPreActionListener({
//                                                        nestedScrollView.scrollBy(0, (commentsView.y).toInt())
//                                                    }, nestedScrollView)
//                                                    .create()
//
//                                        }
//                                    }
//                                }
//                                .create()
//
//                    }
//
//                }, 250)
//
//            }

            if(model.type == DashboardItemType.ReportComplaints) {
                commentsView = binding.cardViewRoot
                commentsViewPosition = position
            }

        }

    }

    fun setPoolCarVehicleCount(count: Int) {
        countPoolCarVehicle = count
        notifyDataSetChanged()
    }

    interface DashboardItemListener {
        fun itemClicked(model: DashboardModel)

        fun highlightCompleted()
    }

}