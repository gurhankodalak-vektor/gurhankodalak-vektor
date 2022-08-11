package com.vektortelekom.android.vservice.data.remote

import com.vektor.ktx.data.remote.ApiHelper
import com.vektortelekom.android.vservice.BuildConfig
import javax.inject.Inject

class AppApiHelper

@Inject
constructor() : ApiHelper {

    override var baseUrl: String
        get() = BuildConfig.BASE_URL
        set(value) {}

    override var baseUrl2: String
        get() = BuildConfig.BASE_URL_2
        set(value) {}

}