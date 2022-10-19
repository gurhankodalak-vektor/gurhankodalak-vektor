package com.vektortelekom.android.vservice.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vektor.ktx.data.local.BaseDataManager
import com.vektor.ktx.utils.DeviceHelper
import com.vektor.ktx.utils.map.VClusterItem
import com.vektor.vcommon.helpers.secure.SharedPreferenceManager
import com.vektor.vshare_api_ktx.model.MobileParameters
import com.vektortelekom.android.vservice.data.model.CustomerStatusModel
import com.vektortelekom.android.vservice.data.model.LocationModel
import com.vektortelekom.android.vservice.data.model.PersonnelModel
import com.vektortelekom.android.vservice.data.model.UserInfoModel
import com.vektortelekom.android.vservice.utils.AppConstants
import java.lang.ref.WeakReference
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

class AppDataManager : BaseDataManager() {

    var customerInfo: UserInfoModel? = null
        set(value) {
            if (value != customerInfo) {
                saveData("customerInfo", value)
                field = value
            }
        }

    var lastRouteSearch: ArrayList<LocationModel>? = ArrayList()
        get() = stringToArray("lastRouteSearch")
        set(value) {
            if (value != lastRouteSearch) {
                saveData("lastRouteSearch", value)
                field = value
            }
        }

    var paymentRequired: Boolean? = null
    var isUpdateSessionCount: Boolean? = true
    var balance: Double? = null
    var personnelInfo: PersonnelModel? = null
    var carShareUser: CustomerStatusModel? = null
    var sameSession: Boolean? = false


    var mobileId: String = ""
    var currentLocation: Location? = null
    var mobileParameters: MobileParameters = MobileParameters(null)
        set(value) {
            if (value != mobileParameters) {
                saveData("mobileParameters", value)
                field = value
            }
        }

    var isShowNotification: Boolean = true

    var isQrAutoOpen: Boolean
        get() = mPrefs.getBoolean(PREF_KEY_QR_AUTO_OPEN, false)
        set(value) = mPrefs.edit().putBoolean(PREF_KEY_QR_AUTO_OPEN, value).apply()

    var isSettingsSoundEffectsEnabled: Boolean
        get() = mPrefs.getBoolean(PREF_KEY_SETTINGS_SOUND_EFFECTS, false)
        set(value) = mPrefs.edit().putBoolean(PREF_KEY_SETTINGS_SOUND_EFFECTS, value).apply()

    var isSettingsNotificationsEnabled: Boolean
        get() = mPrefs.getBoolean(PREF_KEY_SETTINGS_NOTIFICATIONS, false)
        set(value) = mPrefs.edit().putBoolean(PREF_KEY_SETTINGS_NOTIFICATIONS, value).apply()

    var isSettingsEmailNotificationsEnabled: Boolean
        get() = mPrefs.getBoolean(PREF_KEY_SETTINGS_EMAIL_NOTIFICATIONS, false)
        set(value) = mPrefs.edit().putBoolean(PREF_KEY_SETTINGS_EMAIL_NOTIFICATIONS, value).apply()


    fun isHighlightAlreadyShown(key: String): Boolean {
        return mPrefs.getBoolean(PREF_KEY_HIGHLIGHT_PREFIX.plus(key), false)
    }

    fun setHighlightAlreadyShown(key: String) {
        mPrefs.edit().putBoolean(PREF_KEY_HIGHLIGHT_PREFIX.plus(key), true).apply()
    }

    var sessionCount: Int
        get() = mPrefs.getInt(PREF_SESSION_COUNT, 0)
        set(value) = mPrefs.edit().putInt(PREF_SESSION_COUNT, value).apply()

    var tempCount: Int
        get() = mPrefs.getInt(TEMP_COUNT, 3)
        set(value) = mPrefs.edit().putInt(TEMP_COUNT, value).apply()

    var showReview: Boolean
        get() = mPrefs.getBoolean(SHOW_REVIEW, false)
        set(value) = mPrefs.edit().putBoolean(SHOW_REVIEW, value).apply()

    var lastVersion: String?
        get() = mPrefs.getString(LAST_VERSION, "")
        set(value) = mPrefs.edit().putString(LAST_VERSION, value).apply()


    fun restartHighlights() {
        mPrefs.edit().remove(PREF_KEY_HIGHLIGHT_SEQUENCE_PREFIX.plus("sequence_comments_main_fragment"))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("button_add_comment"))
                .remove(PREF_KEY_HIGHLIGHT_SEQUENCE_PREFIX.plus(("sequence_home_activity")))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("home_menu"))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("home_shuttle"))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("home_comments"))
                .remove(PREF_KEY_HIGHLIGHT_SEQUENCE_PREFIX.plus("sequence_shuttle_main"))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("shuttle_info"))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("shuttle_qr"))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("shuttle_button_search_route"))
                .remove(PREF_KEY_HIGHLIGHT_SEQUENCE_PREFIX.plus("sequence_shuttle_from_to"))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("shuttle_stops_marker"))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("shuttle_station_details"))
                .remove(PREF_KEY_HIGHLIGHT_PREFIX.plus("shuttle_station_details_buttons"))
                .apply()
    }

    fun isHighlightSequenceSkipped(key: String): Boolean {
        return mPrefs.getBoolean(PREF_KEY_HIGHLIGHT_SEQUENCE_PREFIX.plus(key), false)
    }

    fun setHighlightSequenceSkipped(key: String) {
        mPrefs.edit().putBoolean(PREF_KEY_HIGHLIGHT_SEQUENCE_PREFIX.plus(key), true).apply()
    }

    var isSettingsShowPhoneEnabled: Boolean
        get() = mPrefs.getBoolean(PREF_KEY_SETTINGS_SHOW_PHONE, false)
        set(value) = mPrefs.edit().putBoolean(PREF_KEY_SETTINGS_SHOW_PHONE, value).apply()

    var googleCalendarAccessToken: String?
        get() = mPrefs.getString(PREF_KEY_GOOGLE_CALENDAR_ACCESS_TOKEN, null)
        set(value) = mPrefs.edit().putString(PREF_KEY_GOOGLE_CALENDAR_ACCESS_TOKEN, value).apply()

    var googleCalendarEmail: String?
        get() = mPrefs.getString(PREF_KEY_GOOGLE_CALENDAR_EMAIL, null)
        set(value) = mPrefs.edit().putString(PREF_KEY_GOOGLE_CALENDAR_EMAIL, value).apply()

    var outlookCalendarAccessToken: String?
        get() = mPrefs.getString(PREF_KEY_OUTLOOK_CALENDAR_ACCESS_TOKEN, null)
        set(value) = mPrefs.edit().putString(PREF_KEY_OUTLOOK_CALENDAR_ACCESS_TOKEN, value).apply()

    var outlookCalendarEmail: String?
        get() = mPrefs.getString(PREF_KEY_OUTLOOK_CALENDAR_EMAIL, null)
        set(value) = mPrefs.edit().putString(PREF_KEY_OUTLOOK_CALENDAR_EMAIL, value).apply()

    var rememberMe: Boolean?
        get() = mPrefs.getBoolean(PREF_KEY_REMEMBER_ME, false)
        set(value) = mPrefs.edit().putBoolean(PREF_KEY_REMEMBER_ME, value?:false).apply()

    var userName: String?
        get() = mPrefs.getString(PREF_KEY_USER_NAME, null)
        set(value) = mPrefs.edit().putString(PREF_KEY_USER_NAME, value).apply()

    var password: String?
        get() = mPrefs.getString(PREF_KEY_PASSWORD, null)
        set(value) = mPrefs.edit().putString(PREF_KEY_PASSWORD, value).apply()

    private lateinit var mPrefs: SharedPreferences
    private val gson = Gson()
    private var isInitializing = false

    @SuppressLint("HardwareIds")
    fun init(appContext: Context) {

        isInitializing = true

        context = WeakReference(appContext)
        mPrefs = SharedPreferenceManager.createSharedPreferences(appContext, AppConstants.System.PREF_DATA_FILE_NAME)

        this.mobileId = DeviceHelper.getDeviceId(appContext)
        this.customerInfo = loadData("customerInfo")
        (loadData("mobileParameters") as MobileParameters?)?.let {
            this.mobileParameters = it
        }

        isInitializing = false
    }

    fun logout() {
        this.customerInfo = null
        this.personnelInfo = null

        mPrefs.edit().remove(PREF_KEY_SETTINGS_SOUND_EFFECTS)
                .remove(PREF_KEY_SETTINGS_NOTIFICATIONS)
                .remove(PREF_KEY_SETTINGS_EMAIL_NOTIFICATIONS)
                .remove(PREF_KEY_SETTINGS_SHOW_PHONE)
                .remove(PREF_KEY_QR_AUTO_OPEN)
                .apply()

    }

    fun setCustomerStatus(value: CustomerStatusModel?) {
        customerInfo = value?.user
        paymentRequired = value?.paymentRequired
        balance = value?.balance
    }

    private fun saveData(key: String, value: Any?) {
        if (isInitializing)
            return
        return mPrefs.edit().putString(key, gson.toJson(value)).apply()
    }

    private inline fun <reified T> loadData(key: String): T? {
        return gson.fromJson(mPrefs.getString(key, null), T::class.java)
    }
    private fun <T> stringToArray(s: String?): ArrayList<T>? {
        val medicineListType: Type = object : TypeToken<ArrayList<LocationModel?>?>() {}.type
        return gson.fromJson(mPrefs.getString(s, null), medicineListType)
    }
    companion object {
        val instance: AppDataManager by lazy { AppDataManager() }

        private const val PREF_KEY_QR_AUTO_OPEN = "PREF_KEY_QR_AUTO_OPEN"
        private const val PREF_KEY_SETTINGS_SOUND_EFFECTS = "PREF_KEY_SETTINGS_SOUND_EFFECTS"
        private const val PREF_KEY_SETTINGS_NOTIFICATIONS = "PREF_KEY_SETTINGS_NOTIFICATIONS"
        private const val PREF_KEY_SETTINGS_EMAIL_NOTIFICATIONS = "PREF_KEY_SETTINGS_EMAIL_NOTIFICATIONS"
        private const val PREF_KEY_SETTINGS_SHOW_PHONE = "PREF_KEY_SETTINGS_SHOW_PHONE"
        private const val PREF_KEY_HIGHLIGHT_PREFIX = "PREF_KEY_HIGHLIGHT_PREFIX_"
        private const val PREF_KEY_HIGHLIGHT_SEQUENCE_PREFIX = "PREF_KEY_HIGHLIGHT_SEQUENCE_PREFIX_"

        private const val PREF_KEY_GOOGLE_CALENDAR_ACCESS_TOKEN = "PREF_KEY_GOOGLE_CALENDAR_ACCESS_TOKEN"
        private const val PREF_KEY_GOOGLE_CALENDAR_EMAIL = "PREF_KEY_GOOGLE_CALENDAR_EMAIL"

        private const val PREF_KEY_OUTLOOK_CALENDAR_ACCESS_TOKEN = "PREF_KEY_OUTLOOK_CALENDAR_ACCESS_TOKEN"
        private const val PREF_KEY_OUTLOOK_CALENDAR_EMAIL = "PREF_KEY_OUTLOOK_CALENDAR_EMAIL"

        private const val PREF_KEY_REMEMBER_ME = "is_remember_me"
        private const val PREF_KEY_USER_NAME = "user_name"
        private const val PREF_KEY_PASSWORD = "password"

        private const val TEMP_COUNT = "TEMP_COUNT"
        private const val PREF_SESSION_COUNT = "SESSION_COUNT"
        private const val SHOW_REVIEW = "SHOW_REVIEW"
        private const val LAST_VERSION = "LAST_VERSION"

    }
}
