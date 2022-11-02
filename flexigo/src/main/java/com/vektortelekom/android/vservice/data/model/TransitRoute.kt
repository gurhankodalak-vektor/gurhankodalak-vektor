package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TransitRoute(
    @SerializedName("durationInMin")
    val durationInMin: Int?,
    @SerializedName("sections")
    val sections: List<Section>?
)
data class Section(
    @SerializedName("arrival")
    val arrival: Place,
    @SerializedName("departure")
    val departure: Place,
    @SerializedName("durationInMin")
    val durationInMin: Int,
    @SerializedName("transport")
    val transport: Transport,
    @SerializedName("type")
    val type: String
): Serializable


data class Transport(
    @SerializedName("category")
    val category: String,
    @SerializedName("headsign")
    val headsign: String,
    @SerializedName("mode")
    val mode: String,
    @SerializedName("name")
    val name: String
): Serializable

data class Place(
    @SerializedName("place")
    val place: Place,
    @SerializedName("time")
    val time: Long,
    @SerializedName("location")
    val location: Location,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String
): Serializable

data class Location(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
): Serializable