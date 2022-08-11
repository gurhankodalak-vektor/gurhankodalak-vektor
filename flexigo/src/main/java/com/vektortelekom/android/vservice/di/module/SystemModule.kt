package com.vektortelekom.android.vservice.di.module

import com.vektor.ktx.data.local.StateManager
import com.vektor.ktx.data.remote.ApiHelper
import com.vektor.ktx.di.annotation.ApiInfo
import com.vektor.ktx.di.annotation.ApiInfo2
import com.vektor.ktx.di.annotation.AppSslPinningInfo
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.data.local.AppStateManager
import com.vektortelekom.android.vservice.data.local.StorageInfo
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.utils.AppConstants
import com.vektortelekom.android.vservice.utils.rx.AppSchedulerProvider
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object SystemModule {

    @JvmStatic
    @Provides
    @ApiInfo
    internal fun provideAppName(): String {
        return BuildConfig.BASE_APP_NAME
    }

    @JvmStatic
    @Provides
    @ApiInfo2
    internal fun provideAppName2(): String {
        return BuildConfig.BASE_APP_NAME_2
    }

    @JvmStatic
    @Provides
    @AppSslPinningInfo
    internal fun provideAppSslPinningInfo(): Boolean {
        return true
    }

    @JvmStatic
    @Provides
    @StorageInfo
    internal fun providePreferenceName(): String {
        return AppConstants.System.PREF_STATE_FILE_NAME
    }

    @JvmStatic
    @Provides
    @Singleton
    internal fun provideStateManager(stateManager: AppStateManager): StateManager {
        return stateManager
    }

    @JvmStatic
    @Provides
    @Singleton
    internal fun provideApiHelper(appApiHelper: AppApiHelper): ApiHelper {
        return appApiHelper
    }

    @JvmStatic
    @Provides
    internal fun provideSchedulerProvider(): SchedulerProvider {
        return AppSchedulerProvider()
    }
}