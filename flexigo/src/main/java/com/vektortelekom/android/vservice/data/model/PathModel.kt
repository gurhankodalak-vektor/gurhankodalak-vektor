package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PathModel (
        @SerializedName("data")
        val data: List<List<Double>>,
        @SerializedName("stations")
        val stations: List<StationModel>
) : Serializable