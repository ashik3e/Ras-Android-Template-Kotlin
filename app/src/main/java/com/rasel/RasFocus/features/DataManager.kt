package com.rasel.RasFocus

import android.content.Context
import android.content.SharedPreferences

object DataManager {
    private const val PREF_NAME = "RasFocusDataCache"
    private lateinit var prefs: SharedPreferences

    // এটি MainActivity এবং Service থেকে কল করে ইনিশিয়ালাইজ করতে হবে
    fun init(context: Context) {
        if (!this::prefs.isInitialized) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    // ==========================================
    // FIX: Public helper methods for prefs access
    // (prefs is private — these replace direct DataManager.prefs access)
    // ==========================================
    fun getBgServiceAck(): Boolean = prefs.getBoolean("bg_service_ack", false)
    fun setBgServiceAck(value: Boolean) = prefs.edit().putBoolean("bg_service_ack", value).apply()

    fun getBatteryOptAck(): Boolean = prefs.getBoolean("battery_opt_ack", false)
    fun setBatteryOptAck(value: Boolean) = prefs.edit().putBoolean("battery_opt_ack", value).apply()

    // ==========================================
    // ১. Simple Blocks & General Settings
    // ==========================================
    var isFocusActive: Boolean
        get() = prefs.getBoolean("isFocusActive", false)
        set(value) = prefs.edit().putBoolean("isFocusActive", value).apply()

    var simpleBlockMode: Int // 0 = Allow, 1 = Block
        get() = prefs.getInt("simpleBlockMode", 1)
        set(value) = prefs.edit().putInt("simpleBlockMode", value).apply()

    var blockSettingsAndUninstall: Boolean
        get() = prefs.getBoolean("blockSettingsAndUninstall", true)
        set(value) = prefs.edit().putBoolean("blockSettingsAndUninstall", value).apply()

    var showQuotes: Boolean
        get() = prefs.getBoolean("showQuotes", true)
        set(value) = prefs.edit().putBoolean("showQuotes", value).apply()

    var quoteLanguage: Int // 0 = Bangla, 1 = English
        get() = prefs.getInt("quoteLanguage", 0)
        set(value) = prefs.edit().putInt("quoteLanguage", value).apply()

    // লিস্ট সেভ করার সুপারফাস্ট টেকনিক (Comma Separated)
    var userAppList: List<String>
        get() = prefs.getString("userAppList", "com.whatsapp,com.facebook.katana")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        set(value) = prefs.edit().putString("userAppList", value.joinToString(",")).apply()

    var userWebList: List<String>
        get() = prefs.getString("userWebList", "facebook.com,tiktok")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        set(value) = prefs.edit().putString("userWebList", value.joinToString(",")).apply()

    // ==========================================
    // ২. Adult Block Settings
    // ==========================================
    var isAdultFocusActive: Boolean
        get() = prefs.getBoolean("isAdultFocusActive", true)
        set(value) = prefs.edit().putBoolean("isAdultFocusActive", value).apply()

    var controlMode: Int // 0 = Self, 1 = Friend
        get() = prefs.getInt("controlMode", 0)
        set(value) = prefs.edit().putInt("controlMode", value).apply()

    var adultReligion: Int // 0=Muslim, 1=Hindu, 2=Christian, 3=Universal
        get() = prefs.getInt("adultReligion", 0)
        set(value) = prefs.edit().putInt("adultReligion", value).apply()

    var adultLanguage: Int
        get() = prefs.getInt("adultLanguage", 0)
        set(value) = prefs.edit().putInt("adultLanguage", value).apply()

    var cleanStreakDays: Int
        get() = prefs.getInt("cleanStreakDays", 12)
        set(value) = prefs.edit().putInt("cleanStreakDays", value).apply()

    var totalBlockedCount: Int
        get() = prefs.getInt("totalBlockedCount", 0)
        set(value) = prefs.edit().putInt("totalBlockedCount", value).apply()

    var userCustomAdultKeywords: List<String>
        get() = prefs.getString("customAdultKeywords", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        set(value) = prefs.edit().putString("customAdultKeywords", value.joinToString(",")).apply()

    var is24HourLockActive: Boolean
        get() = prefs.getBoolean("is24HourLockActive", false)
        set(value) = prefs.edit().putBoolean("is24HourLockActive", value).apply()

    var lock24hEndTime: Long
        get() = prefs.getLong("lock24hEndTime", 0L)
        set(value) = prefs.edit().putLong("lock24hEndTime", value).apply()

    var isPeriodicPopupsActive: Boolean
        get() = prefs.getBoolean("isPeriodicPopupsActive", false)
        set(value) = prefs.edit().putBoolean("isPeriodicPopupsActive", value).apply()

    // ==========================================
    // ৩. Deep Study (Pomodoro) Settings
    // ==========================================
    var isDeepStudyStrict: Boolean
        get() = prefs.getBoolean("isDeepStudyStrict", false)
        set(value) = prefs.edit().putBoolean("isDeepStudyStrict", value).apply()

    var dsFocusMin: Int
        get() = prefs.getInt("dsFocusMin", 25)
        set(value) = prefs.edit().putInt("dsFocusMin", value).apply()

    var dsRestMin: Int
        get() = prefs.getInt("dsRestMin", 5)
        set(value) = prefs.edit().putInt("dsRestMin", value).apply()

    var dsKeepBlockingInBreak: Boolean
        get() = prefs.getBoolean("dsKeepBlockingInBreak", false)
        set(value) = prefs.edit().putBoolean("dsKeepBlockingInBreak", value).apply()

    var dsAllowAppList: List<String>
        get() = prefs.getString("dsAllowAppList", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        set(value) = prefs.edit().putString("dsAllowAppList", value.joinToString(",")).apply()

    var dsAllowWebList: List<String>
        get() = prefs.getString("dsAllowWebList", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        set(value) = prefs.edit().putString("dsAllowWebList", value.joinToString(",")).apply()
}
