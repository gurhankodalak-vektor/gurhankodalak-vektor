package com.vektortelekom.android.vservice.utils

import android.location.Location
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest

class AutoCompleteManager {

    fun getAutoCompleteRequest(key: String, origin: Location?): FindAutocompletePredictionsRequest {
        val token = AutocompleteSessionToken.newInstance()
        var request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(key)
            .build()

        origin?.let {
            val bounds = BoundingBoxUtil().getBoundingBox(it, 5.0)
            request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(key)
                .setLocationBias(bounds)
              //  .setLocationRestriction(bounds)
                .build()
        }
        return  request
    }

    companion object {
        val instance: AutoCompleteManager by lazy { AutoCompleteManager() }

    }
}

