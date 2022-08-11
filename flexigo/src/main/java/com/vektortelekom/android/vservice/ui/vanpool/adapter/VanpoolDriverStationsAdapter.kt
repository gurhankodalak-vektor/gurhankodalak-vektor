package com.vektortelekom.android.vservice.ui.vanpool.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.PersonsModel
import com.vektortelekom.android.vservice.data.model.RouteResponse
import com.vektortelekom.android.vservice.data.model.StationModel
import com.vektortelekom.android.vservice.databinding.VanpoolDriverStationsListItemBinding
import kotlinx.android.extensions.LayoutContainer
import org.json.JSONObject
import java.lang.reflect.Type


class VanpoolDriverStationsAdapter(val listener: VanpoolDriverStationsItemClickListener): RecyclerView.Adapter<VanpoolDriverStationsAdapter.ViewHolder>() {

    private var driverRouteInfo: RouteResponse? = null
    private var driverStations: List<StationModel> = listOf()
    var personsHashmap = HashMap<String, List<PersonsModel>?>(101)  //stationId - persons
    var stationCount = 0
    var persons: MutableList<PersonsModel> = mutableListOf()
    var jsonObject : JSONObject? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VanpoolDriverStationsAdapter.ViewHolder {
        val binding = VanpoolDriverStationsListItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return driverStations.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(driverStations[position])
    }

    inner class ViewHolder (val binding: VanpoolDriverStationsListItemBinding) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

        fun bind(model: StationModel) {
            var ridersCount = 0
            stationCount += 1
            persons = mutableListOf()

            binding.textviewStationName.text = model.title
            binding.textviewStationAddress.text = model.location.address

            if (driverRouteInfo != null)
            {
                val type: Type = object : TypeToken<Map<String?, Int?>?>() {}.type

                val pathPersonnelStations = Gson().toJson(driverRouteInfo!!.pathPersonnelStations, type)
                val returnPathPersonnelStations = Gson().toJson(driverRouteInfo!!.returnPathPersonnelStations, type)

                //default olarak içerideki {} karakterlerini de sayıyor.
                var temp = if (pathPersonnelStations.length > 2) pathPersonnelStations else returnPathPersonnelStations


                val myMap: Map<String, Int> = Gson().fromJson(temp, type)

                myMap.forEach {
                    if (it.value.toLong() == model.id){
                        driverRouteInfo!!.persons.forEach{ personnel ->
                            if (personnel!!.id == it.key.toLong()){
                                persons.add(personnel)
                                ridersCount += 1
                            }
                        }
                    }
                }
            }

            personsHashmap[model.id.toString()] = persons

            binding.textviewCount.text = stationCount.toString()
            binding.textviewStationRiders.text = ridersCount.toString().plus(" ").plus(containerView.context?.getString(R.string.riders))

            binding.imageviewNavigation.setOnClickListener {
                listener.onNavigateClick(model)
            }
            binding.layoutRidersCount.setOnClickListener {
                listener.onPersonsClick(personsHashmap[model.id.toString()])
            }

        }

        override val containerView: View
            get() = binding.root

    }


    interface VanpoolDriverStationsItemClickListener {
        fun onNavigateClick(model: StationModel)
        fun onPersonsClick(model: List<PersonsModel>?)
    }

    fun setDriverInfo(driverRouteInfo: RouteResponse) {
        this.driverRouteInfo = driverRouteInfo
        notifyDataSetChanged()
    }
    fun setStationList(driverStations: List<StationModel>) {
        this.driverStations = driverStations
        notifyDataSetChanged()
    }

}