package com.vektortelekom.android.vservice.data.model

import com.google.gson.annotations.SerializedName

data class CancelRouteReservationsRequest(
    @SerializedName("workgroupInstanceId")
    val workgroupInstanceId: Long,

    @SerializedName("destinationId")
    val destinationId: Long?,

    @SerializedName("routeId")
    val routeId: Long

)


