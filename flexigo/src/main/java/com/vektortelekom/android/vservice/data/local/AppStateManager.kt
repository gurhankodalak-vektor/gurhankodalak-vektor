package com.vektortelekom.android.vservice.data.local

import android.content.Context
import android.content.SharedPreferences
import com.vektor.ktx.data.local.StateManager
import com.vektor.ktx.utils.secure.SharedPreferenceManager
import javax.inject.Inject
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class StorageInfo

class AppStateManager

@Inject
constructor(context: Context, @StorageInfo fileName: String) : StateManager {

    private var mPrefs: SharedPreferences = SharedPreferenceManager.createSharedPreferences(context, fileName)

    private var _otpToken: String? = null
    override var otpToken: String?
        get() = _otpToken
        set(value) {_otpToken = value}

    override var accessToken: String?
        get() = mPrefs.getString(PREF_KEY_ACCESS_TOKEN, null)
        set(value) = mPrefs.edit().putString(PREF_KEY_ACCESS_TOKEN, value).apply()

    override var vektorToken: String?
        get() = mPrefs.getString(PREF_KEY_VEKTOR_TOKEN, null)
        set(value) = mPrefs.edit().putString(PREF_KEY_VEKTOR_TOKEN, value).apply()

    override var firstRun: Boolean
        get() = mPrefs.getBoolean(PREF_KEY_FIRST_RUN, true)
        set(value) = mPrefs.edit().putBoolean(PREF_KEY_FIRST_RUN, value).apply()

    override var isLoggedIn: Boolean
        get() = !mPrefs.getString(PREF_KEY_VEKTOR_TOKEN, null).isNullOrEmpty()
        set(value) {}

    private var _otpValidSeconds: Int? = null
    override var otpValidSeconds: Int?
        get() = _otpValidSeconds
        set(value) {_otpValidSeconds = value}

    override fun logout() {
        this.otpToken = null
        this.accessToken = null
        this.vektorToken = null
    }

    override var baseURL: String?
        get() = mPrefs.getString(PREF_KEY_FLEXIGO_APP_URL, null)
        set(value) {
            mPrefs.edit().putString(PREF_KEY_FLEXIGO_APP_URL, value).apply()
        }

    companion object {
        private const val PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN"
        private const val PREF_KEY_VEKTOR_TOKEN = "PREF_KEY_VEKTOR_TOKEN"
        private const val PREF_KEY_FIRST_RUN = "PREF_KEY_FIRST_RUN"
        private const val PREF_KEY_HAS_FIRST_REQUEST = "PREF_KEY_HAS_FIRST_REQUEST"
        private const val PREF_KEY_FLEXIGO_APP_URL = "PREF_KEY_FLEXIGO_APP_URL"
    }
}