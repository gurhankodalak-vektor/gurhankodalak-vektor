package com.vektortelekom.android.vservice.utils

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vektor.ktx.utils.logger.AppLogger

class AnalyticsManager private constructor(private val context: Context) {

    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val firebaseCrashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    fun setUserId(userId: String): AnalyticsManager {
        try {
            firebaseAnalytics.setUserId(userId)
            firebaseCrashlytics.setUserId(userId)
        } catch (e: Exception) {
            AppLogger.e(e, "Firebase setUserId failed")
        }
        return this
    }

    fun sendFragmentScreenEvent(fragment: Fragment) {
        try{
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, fragment.javaClass.simpleName)
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, fragment.javaClass.simpleName)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }
        catch (e: Exception) {
            AppLogger.e(e, "Firebase Analytics sendFragmentScreenEvent failed")
        }
    }



    companion object {

        fun build(context: Context): AnalyticsManager {
            return AnalyticsManager(context)
        }
    }
}