package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import com.vektortelekom.android.vservice.ui.poolcar.reservation.adapter.PoiListAdapter

data class PoiModel(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("identity")
        val identity: String,
        @SerializedName("latitude")
        val latitude: Double,
        @SerializedName("longitude")
        val longitude: Double,
        @SerializedName("type")
        val type: String
): PoiListAdapter.PoiListItem