package com.vektortelekom.android.vservice.data.repository

import com.vektor.vshare_api_ktx.service.ConstantsService
import com.vektor.vshare_api_ktx.service.VersionService
import javax.inject.Inject

class MobileRepository
@Inject
constructor(private val versionService: VersionService,
            private val constantsService: ConstantsService
) {

    fun checkVersion(appName: String, platform: String) = versionService.checkVersionV2(appName, platform)

    fun getMobileParameters(language: String) = constantsService.getMobileParameters(language)

}