package com.vektortelekom.android.vservice

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LifecycleObserver
import androidx.multidex.MultiDex
import com.google.android.libraries.places.api.Places
import com.vektor.ktx.utils.ImageHelper
import com.vektor.ktx.utils.TextHelper
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.di.component.DaggerApplicationComponent
import dagger.android.DaggerApplication
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import net.danlew.android.joda.JodaTimeAndroid

class MainApp : DaggerApplication(), LifecycleObserver {

    private val applicationInjector = DaggerApplicationComponent.builder()
            .application(this)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this

        AppLogger.init()

        AppDataManager.instance.init(this.applicationContext)

        JodaTimeAndroid.init(this)

        val calligraphyConfig = CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/SFProDisplay-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        val viewPump = ViewPump.builder().addInterceptor(CalligraphyInterceptor(calligraphyConfig)).build()
        ViewPump.init(viewPump)

        if(BuildConfig.FLAVOR == "tums") {
            Places.initialize(applicationContext, TextHelper.d("pxK2\$Jrp(f}bxdvbfy0AGK~fg?8F>#b|@>?sFAx", "tumsxx")!!)
        }
        else {
            Places.initialize(applicationContext, TextHelper.d("pxK2\$Jp\$4bydp`pbFe>>(f|%+7Bq3<?vFG`K&a&", "flexit")!!)
        }


        ImageHelper.startImageFolderCleanupTask(this)

        // For Android 21
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun applicationInjector() = applicationInjector

    companion object {
        lateinit var instance: MainApp private set
        private val TAG = MainApp::class.java.simpleName
    }
}