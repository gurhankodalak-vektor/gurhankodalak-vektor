package com.vektortelekom.android.vservice.ui.shuttle.model

import com.google.android.gms.maps.model.LatLng

data class VPlaceModel (
        val name: String,
        val latLng: LatLng,
        val isCampus: Boolean,
        val id: Long?
)