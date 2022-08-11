package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektortelekom.android.vservice.ui.poolcar.reservation.adapter.PoiListAdapter
import java.io.Serializable

data class ParkModel (
        @SerializedName("id")
        var id: Int,
        @SerializedName("name")
        val name: String? = null,
        @SerializedName("address")
        val address: String? = null,
        @SerializedName("vehicleAvailableCount")
        val vehicleAvailableCount: Int? = null,
        @SerializedName("vehicleCount")
        val vehicleCount: Int? = null,
        @SerializedName("city")
        val city: String? = null,
        @SerializedName("district")
        val district: String? = null,
        @SerializedName("latitude")
        val latitude: Double? = null,
        @SerializedName("longitude")
        val longitude: Double? = null,
        @SerializedName("workingHours")
        val workingHours: String? = null,
        @SerializedName("location")
        val location: DestinationModel? = null,
        @SerializedName("province")
        val province: String? = null,
        @SerializedName("poiId")
        val poiId: Int? = null
): Serializable, PoiListAdapter.PoiListItem