package com.vektortelekom.android.vservice.ui.poolcar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.StationVehicleModel
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.PoolCarCarModelsViewHolderItemBinding
import com.vektortelekom.android.vservice.utils.GlideApp
import com.vektortelekom.android.vservice.utils.PhotoHelper
import kotlinx.android.extensions.LayoutContainer

class ParkingCarModelsAdapter(private val carModels: List<StationVehicleModel>, private val listener: CarModelListener?) : RecyclerView.Adapter<ParkingCarModelsAdapter.ParkingCarModelsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkingCarModelsViewHolder {
        val binding = PoolCarCarModelsViewHolderItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ParkingCarModelsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return carModels.size
    }

    override fun onBindViewHolder(holder: ParkingCarModelsViewHolder, position: Int) {
        holder.bind(carModels[position])
    }

    inner class ParkingCarModelsViewHolder(val binding: PoolCarCarModelsViewHolderItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {
        override val containerView: View
            get() = binding.root

        fun bind(carModel: StationVehicleModel) {

            binding.textViewCarName.text = carModel.vehicle.make.plus(" ").plus(carModel.vehicle.model)

            when(carModel.vehicle.transmissionType) {
                "otomatik" -> {
                    binding.textViewGear.text = containerView.context.getString(R.string.automatic)
                }
                "manuel"-> {
                    binding.textViewGear.text = containerView.context.getString(R.string.manual)
                }
                else -> {
                    binding.textViewGear.text = carModel.vehicle.transmissionType
                }
            }


            when(carModel.vehicle.fuelType) {
                "benzinli" -> {
                    binding.textViewFuel.text = containerView.context.getString(R.string.gasoline)
                }
                "dizel" -> {
                    binding.textViewFuel.text = containerView.context.getString(R.string.diesel)
                }
                else -> {
                    binding.textViewFuel.text = carModel.vehicle.fuelType
                }
            }


            binding.cardViewCarModel.setOnClickListener {
                listener?.carModelClicked(carModel)
            }

            if(carModel.imageUuid.isNullOrEmpty().not()) {
                val url: String = AppApiHelper().baseUrl2
                        .plus("/")
                        .plus("report/fileViewer/uuid/")
                        .plus(carModel.imageUuid)

                val requestOptions = RequestOptions()
                        .placeholder(R.drawable.placeholder_black)
                        .error(R.drawable.placeholder_black)

                GlideApp.with(itemView.context.applicationContext).setDefaultRequestOptions(requestOptions).load(url).into(binding.imageViewCar)
            } else if (carModel.vehicle.imageName.isNullOrEmpty()) {
                binding.imageViewCar.setImageResource(R.drawable.ic_car_icon)
            } else {
                PhotoHelper.loadCarImageToImageViewWithCache(itemView.context.applicationContext, carModel.vehicle.imageName, binding.imageViewCar, false)
            }

        }

    }

    interface CarModelListener {
        fun carModelClicked(carModel: StationVehicleModel)
    }

}