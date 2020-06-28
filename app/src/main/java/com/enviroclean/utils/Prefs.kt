package com.enviroclean.utils

import android.content.Context
import android.content.SharedPreferences
import com.enviroclean.model.*
import com.google.gson.Gson

class Prefs(context: Context) {

    private val PREF_USER_DATA = "userData"
    private val PREF_USER_DETAILS = "userData_details"
    private val PREF_AREA_LIST_ADDRESS = "area_list_aadress"
    private val PREF_CHECK_IN_DATA = "check_in_data"
    private val PREF_ACCESS_TOKEN = "accessToken"
    private val PREF_DEVICE_TOKEN = "deviceToken"
    private val PREFS_REMINDERS = "REMINDERS"
    private val PREFS_REMEMBER_ME = "Remember_me"
    private val PREFS_QR_CODE_ME = "Remember_me_qr_code"
    private val PREFS_PERMANENTLY = "permanently"
    private val PREFS_EMAIL_OR_PHONE = "email_or_phone"
    private val PREFS_FIREBASE_TOKEN = "firebse_token"
    private val VIOLATION_AREA_ID = "violation_area_id"
    private val VIOLATION_AREA_SIZE = "violation_area_size"
    private val PREFS_USER_EMAIL = "user_email"
    private val PREFS_NOTIFICATION_COUNT = "pref_notification_count"
    private val PREFS_USER_PASSWORD = "user_password"
    private val PREFS_LAST_QR_ID = "last_qr_id"
    private val PREFS_LAST_SCAN_QR_ID = "last_qr_scan_id"
    private val PREFS_PASSWORD = "password"
    private val PREFS_CURRENT_PASSWORD = "current_password"
    private val PREFS_CHECK_IN= "ischeckin"
    private val PREFS_TWILIO_ACCESS_TOKEN="Twilio_access_token"


    private val SP_NAME = Prefs::class.java.name
    private val SP_NAME_RememberMe = "remember_me"
    private val IS_CHACK_IN = "is_checking"
    private var sharedPreferences: SharedPreferences? = null
    private var sharedPreferencesIsCheckIn: SharedPreferences? = null
    private var sharedPreferencesRememberMe: SharedPreferences? = null
    private var sharedPreferencesQRCode: SharedPreferences? = null
    private var sharedPreferencesPermanently: SharedPreferences? = null

    init {
        sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        sharedPreferencesRememberMe = context.getSharedPreferences(SP_NAME_RememberMe, Context.MODE_PRIVATE)
        sharedPreferencesIsCheckIn = context.getSharedPreferences(IS_CHACK_IN, Context.MODE_PRIVATE)
        sharedPreferencesQRCode = context.getSharedPreferences(PREFS_QR_CODE_ME, Context.MODE_PRIVATE)
        sharedPreferencesPermanently = context.getSharedPreferences(PREFS_PERMANENTLY, Context.MODE_PRIVATE)
    }

    var isLoggedIn: Boolean
        set(value) = sharedPreferences!!.edit().putBoolean("key", value).apply()
        get() = sharedPreferences!!.getBoolean("key", false)

    var isTouchId: Boolean
        set(isTouchId) = sharedPreferencesPermanently!!.edit().putBoolean("isTouchId", isTouchId).apply()
        get() = sharedPreferencesPermanently!!.getBoolean("isTouchId", false)


    var userEmail: String?
        get() = sharedPreferencesPermanently!!.getString(PREFS_USER_EMAIL, "")
        set(userEmail) = sharedPreferencesPermanently!!.edit().putString(PREFS_USER_EMAIL, userEmail!!).apply()

    var notificationCount: String?
        get() = sharedPreferencesPermanently!!.getString(PREFS_NOTIFICATION_COUNT, "")
        set(notificationCount) = sharedPreferencesPermanently!!.edit().putString(PREFS_NOTIFICATION_COUNT, notificationCount!!).apply()

    var userPassword: String?
        get() = sharedPreferencesPermanently!!.getString(PREFS_USER_PASSWORD, "")
        set(userPassword) = sharedPreferencesPermanently!!.edit().putString(PREFS_USER_PASSWORD, userPassword!!).apply()


    var isTutorialIn: Boolean
        set(value) = sharedPreferencesRememberMe!!.edit().putBoolean("tutorial", value).apply()
        get() = sharedPreferencesRememberMe!!.getBoolean("tutorial", false)

    var userDataModel: LoginResponse1?
        get() = Gson().fromJson<LoginResponse1>(sharedPreferences!!.getString(PREF_USER_DATA, ""), LoginResponse1::class.java)
        set(userDataModel) = sharedPreferences!!.edit().putString(PREF_USER_DATA, Gson().toJson(userDataModel)).apply()

     var userDetails: UserDetailsResponse?
        get() = Gson().fromJson<UserDetailsResponse>(sharedPreferences!!.getString(PREF_USER_DETAILS, ""), UserDetailsResponse::class.java)
        set(userDetails) = sharedPreferences!!.edit().putString(PREF_USER_DETAILS, Gson().toJson(userDetails)).apply()

      var areaList: ViolationList?
        get() = Gson().fromJson<ViolationList>(sharedPreferences!!.getString(PREF_AREA_LIST_ADDRESS, ""), ViolationList::class.java)
        set(areaList) = sharedPreferences!!.edit().putString(PREF_AREA_LIST_ADDRESS, Gson().toJson(areaList)).apply()

    var accessToken: String?
        get() = sharedPreferences!!.getString(PREF_ACCESS_TOKEN, "")
        set(accessToken) = sharedPreferences!!.edit().putString(PREF_ACCESS_TOKEN, accessToken).apply()

    var userDetailsUpdate: Boolean
        set(value) = sharedPreferences!!.edit().putBoolean("isUpdate", value).apply()
        get() = sharedPreferences!!.getBoolean("isUpdate", false)

    var deviceToken: String?
        get() = sharedPreferences!!.getString(PREF_DEVICE_TOKEN, "")
        set(deviceToken) = sharedPreferences!!.edit().putString(PREF_DEVICE_TOKEN, deviceToken).apply()

    /* Remember Me */
    var rememberMe: Boolean?
        get() = sharedPreferencesRememberMe!!.getBoolean(PREFS_REMEMBER_ME, false)
        set(rememberMe) = sharedPreferencesRememberMe!!.edit().putBoolean(PREFS_REMEMBER_ME, rememberMe!!).apply()

    var firebaseToken: String?
        get() = sharedPreferencesRememberMe!!.getString(PREFS_FIREBASE_TOKEN, "")
        set(firebaseToken) = sharedPreferencesRememberMe!!.edit().putString(PREFS_FIREBASE_TOKEN, firebaseToken!!).apply()

    var emailOrPhone: String?
        get() = sharedPreferencesRememberMe!!.getString(PREFS_EMAIL_OR_PHONE, "")
        set(emailPhone) = sharedPreferencesRememberMe!!.edit().putString(PREFS_EMAIL_OR_PHONE, emailPhone!!).apply()

    var violationAreaId: String?
        get() = sharedPreferencesIsCheckIn!!.getString(VIOLATION_AREA_ID, "")
        set(violationAreaId) = sharedPreferencesIsCheckIn!!.edit().putString(VIOLATION_AREA_ID, violationAreaId!!).apply()

    var violationAreaSize: String?
        get() = sharedPreferencesIsCheckIn!!.getString(VIOLATION_AREA_SIZE, "")
        set(violationAreaSize) = sharedPreferencesIsCheckIn!!.edit().putString(VIOLATION_AREA_SIZE, violationAreaSize!!).apply()

    var password: String?
        get() = sharedPreferencesRememberMe!!.getString(PREFS_PASSWORD, "")
        set(password) = sharedPreferencesRememberMe!!.edit().putString(PREFS_PASSWORD, password!!).apply()

        var currentPassword: String?
        get() = sharedPreferences!!.getString(PREFS_CURRENT_PASSWORD, "")
        set(currentPassword) = sharedPreferences!!.edit().putString(PREFS_CURRENT_PASSWORD, currentPassword!!).apply()

    var isCheckIn: String?
        get() = sharedPreferencesIsCheckIn!!.getString(PREFS_CHECK_IN, "")
        set(isCheckIn) = sharedPreferencesIsCheckIn!!.edit().putString(PREFS_CHECK_IN, isCheckIn!!).apply()

    var isCheck: Boolean
        set(value1) = sharedPreferencesIsCheckIn!!.edit().putBoolean("isCheck", value1).apply()
        get() = sharedPreferencesIsCheckIn!!.getBoolean("isCheck", false)

    var clickQrCodeIdValets: String?
        get() = sharedPreferencesQRCode!!.getString(PREFS_LAST_QR_ID, "")
        set(lastQrCodeIdValets) = sharedPreferencesQRCode!!.edit().putString(PREFS_LAST_QR_ID, lastQrCodeIdValets!!).apply()

    var lastScanQrCodeIdValets: String?
        get() = sharedPreferencesQRCode!!.getString(PREFS_LAST_SCAN_QR_ID, "")
        set(lastScanQrCodeIdValets) = sharedPreferencesQRCode!!.edit().putString(PREFS_LAST_SCAN_QR_ID, lastScanQrCodeIdValets!!).apply()

    var clickQrCodeIdMangerValets: String?
        get() = sharedPreferencesQRCode!!.getString(PREFS_LAST_QR_ID, "")
        set(lastQrCodeIdMangerValets) = sharedPreferencesQRCode!!.edit().putString(PREFS_LAST_QR_ID, lastQrCodeIdMangerValets!!).apply()

    var lastScanQrCodeIdMangerValets: String?
        get() = sharedPreferencesQRCode!!.getString(PREFS_LAST_SCAN_QR_ID, "")
        set(lastScanQrCodeIdMangerValets) = sharedPreferencesQRCode!!.edit().putString(PREFS_LAST_SCAN_QR_ID, lastScanQrCodeIdMangerValets!!).apply()

    var twilioAccessToken: String?
        get() = sharedPreferences!!.getString(PREFS_TWILIO_ACCESS_TOKEN, "")
        set(twilioAccessToken) = sharedPreferences!!.edit().putString(PREFS_TWILIO_ACCESS_TOKEN, twilioAccessToken!!).apply()
    /*    var reminders: List<ReminderData>?
          get() = Gson().fromJson(sharedPreferences!!.getString(PREFS_REMINDERS, ""), Array<ReminderData>::class.java)?.toList()
          set(reminders) = sharedPreferences!!.edit().putString(PREFS_REMINDERS, Gson().toJson(reminders)).apply()*/
    fun clearPrefs() {
        clearCheckinDate()
        sharedPreferences!!.edit().clear().apply()
    }
    fun clearCheckinDate(){
        sharedPreferencesIsCheckIn!!.edit().clear().apply()
        sharedPreferencesQRCode!!.edit().clear().apply()
    }
    fun clearLastStoreQRID(){
        sharedPreferencesQRCode!!.edit().clear().apply()
    }
    /*[start]check in data*/
    var checkingResponse: CheckIngResponse1?

        get() = Gson().fromJson<CheckIngResponse1>(
            sharedPreferencesIsCheckIn!!.getString(AppConstants.CHECKINDATA, ""),
            CheckIngResponse1::class.java
        )

        set(checkingResponse) = sharedPreferencesIsCheckIn!!.edit().putString(PREF_CHECK_IN_DATA, Gson().toJson(checkingResponse)).apply()

    /*[end]check in data*/
    /*Save value to preference via key value pair*/
    fun writeString(key: String, value: String) {
        if(sharedPreferences != null) {
            val prefsEditor: SharedPreferences.Editor = sharedPreferences!!.edit()
            with(prefsEditor) {
                putString(key, value)
                apply()
            }
        }
    }

    fun writeBoolean(key: String, value: Boolean) {
        if(sharedPreferences != null) {
            val prefsEditor: SharedPreferences.Editor = sharedPreferences!!.edit()
            with(prefsEditor) {
                putBoolean(key, value)
                apply()
            }
        }
    }

    fun writeInt(key: String, value: Int) {
        if(sharedPreferences != null) {
            val prefsEditor: SharedPreferences.Editor = sharedPreferences!!.edit()
            with(prefsEditor) {
                putInt(key, value)
                apply()
            }
        }
    }

    /*Get int value from preference*/
    fun readInt(key: String): Int? {
        return sharedPreferences?.getInt(key, 0)
    }

    /*Get int value from preference*/
    fun readString(key: String): String {
        return if(sharedPreferences != null) {
            sharedPreferences!!.getString(key, "")!!
        } else {
            ""
        }
    }

    fun readBoolean(key: String): Boolean {
        return if(sharedPreferences != null) {
            sharedPreferences!!.getBoolean(key, false)
        } else {
            return false
        }
    }

    companion object {
        var prefs: Prefs? = null

        fun getInstance(context: Context): Prefs? {
            prefs = if (prefs != null) prefs else Prefs(context)
            return prefs
        }
    }
}