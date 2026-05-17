package com.rasel.RasFocus.features

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.rasel.RasFocus.DataManager

class BlockerAccessibilityService : AccessibilityService() {

    companion object {
        var instance: BlockerAccessibilityService? = null
        private const val NOTIFICATION_CHANNEL_ID = "RasFocus_Channel"
        private const val NOTIFICATION_ID = 1001
    }

    private val hardcoreKeywords = listOf(
        "porn", "xxx", "sex", "nude", "nsfw", "sexy", "hentai", "rule34", "milf",
        "blowjob", "tits", "boobs", "pussy", "dick", "cock", "escort", "bdsm",
        "fetish", "erotica", "dildo", "webcam", "camgirls", "xvideos", "pornhub",
        "xnxx", "xhamster", "brazzers", "onlyfans", "playboy", "chaturbate",
        "stripchat", "eporner", "spankbang", "redtube", "youporn", "mia khalifa",
        "sunny leone", "dani daniels", "johnny sins", "kendra lust",
        "চটি", "পর্ণ", "সেক্স", "নগ্ন", "উলঙ্গ", "বেশ্যা", "মাগি", "খানকি",
        "যৌন", "পর্ণগ্রাফি", "রেন্ডি", "চোদাচুতি", "গরম ভিডিও", "খারাপ ছবি",
        "যৌন মিলন", "যৌনাঙ্গ", "চুদো", "নগ্নতা"
    )

    private val romanticKeywords = listOf(
        "hot dance", "seductive dance", "item song", "belly dance", "hot",
        "kissing scene", "bikini", "swimsuit", "sexy dance", "cleavage", "hot scene",
        "romantic kiss", "bedroom scene", "bath scene", "rain dance", "bold scene",
        "semi nude", "lingerie", "erotic", "hot song", "romantic video hot",
        "navel show", "deep neck", "short dress sexy", "unfaithful scene"
    )

    private val adultWebsites = listOf(
        "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "redtube.com",
        "youporn.com", "brazzers.com", "spankbang.com", "eporner.com", "chaturbate.com"
    )

    private var dynamicAdultList = listOf<String>()

    private var lastBlockTime: Long = 0
    private var isDeepStudyActive = false
    private var isDeepStudyBreak = false

    private var windowManager: android.view.WindowManager? = null
    private var dsTimer: android.os.CountDownTimer? = null
    private var dsTimeLeftMillis: Long = 0
    private var floatingTimerView: android.view.View? = null
    private var timerTextView: android.widget.TextView? = null
    private var breakScreenView: android.view.View? = null
    private var sessionCompleteView: android.view.View? = null
    private var fullScreenBlockView: android.view.View? = null

    private var audioTrack: android.media.AudioTrack? = null
    private var isPlayingNoise = false
    private var noiseThread: Thread? = null

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    private lateinit var recoveryPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        DataManager.init(this)
        recoveryPrefs = getSharedPreferences("FocusRecovery", Context.MODE_PRIVATE)
        createNotificationChannel()
        loadAdultSiteFile()
    }

    private fun loadAdultSiteFile() {
        try {
            val inputStream = assets.open("adultsite.txt")
            val text = inputStream.bufferedReader().use { it.readText() }
            dynamicAdultList = text.split("\n", "\r\n")
                .map { it.trim().lowercase().replace("*.", ".") }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            android.util.Log.e("RasFocus", "Error reading adultsite.txt: ${e.message}")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        this.serviceInfo = info

        startForeground(NOTIFICATION_ID, buildNotification("Protection is Active", "Monitoring your focus..."))

        val isSavedActive = recoveryPrefs.getBoolean("isTimerActive", false)
        val targetEndTime = recoveryPrefs.getLong("targetEndTime", 0L)
        val sessionType = recoveryPrefs.getInt("sessionType", 0)
        val soundType = recoveryPrefs.getInt("soundType", 0)
        val playSound = recoveryPrefs.getBoolean("playSound", false)

        if (isSavedActive && targetEndTime > System.currentTimeMillis()) {
            val remainingMillis = targetEndTime - System.currentTimeMillis()
            if (sessionType == 0) {
                resumeDeepStudySession(remainingMillis, playSound, soundType)
            } else {
                startDeepStudyBreak((remainingMillis / 60000).toInt())
            }
        } else {
            recoveryPrefs.edit().clear().apply()
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopAmbientSound()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "RasFocus Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the app running in background to ensure focus protection."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(title, content))
    }

    private fun isSystemApp(packageName: String): Boolean {
        return packageName.contains("launcher") ||
                packageName.contains("systemui") ||
                packageName.contains("dialer") ||
                packageName.contains("telecom") ||
                packageName.contains("messaging") ||
                packageName.contains("mms") ||
                packageName.contains("contacts") ||
                packageName.contains("inputmethod") ||
                packageName.contains("keyboard") ||
                packageName == "com.rasel.RasFocus"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || (!DataManager.isFocusActive && !DataManager.isAdultFocusActive && !isDeepStudyActive)) return

        val packageName = event.packageName?.toString() ?: return

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val source = event.source
            val typedText = event.text.joinToString(" ").lowercase()
            val isDynamicTypingMatch = dynamicAdultList.any { typedText.contains(it) }

            if (!isSystemApp(packageName) && DataManager.isAdultFocusActive &&
                (hardcoreKeywords.any { typedText.contains(it) } || isDynamicTypingMatch)
            ) {
                source?.let { node ->
                    val selectArgs = android.os.Bundle()
                    selectArgs.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0)
                    selectArgs.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, typedText.length)
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectArgs)

                    val clearArgs = android.os.Bundle()
                    clearArgs.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearArgs)
                }
                triggerAdultBlockAction(packageName, "Explicit Keyword Typed")
                return
            }
        }
    }

    override fun onInterrupt() {
        // Required override — intentionally empty
    }

    // ---------- Stub functions (implement as needed) ----------

    fun tryStopFocus(password: String): Boolean {
        // Implement password check logic
        return false
    }

    fun startDeepStudySession(focusMin: Int, playSound: Boolean) {
        isDeepStudyActive = true
        // Implement timer + overlay logic
    }

    fun resumeDeepStudySession(remainingMillis: Long, playSound: Boolean, soundType: Int) {
        isDeepStudyActive = true
        // Implement resume logic
    }

    fun startDeepStudyBreak(breakMin: Int) {
        isDeepStudyBreak = true
        // Implement break overlay logic
    }

    fun stopAmbientSound() {
        isPlayingNoise = false
        noiseThread?.interrupt()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    private fun triggerAdultBlockAction(packageName: String, reason: String) {
        android.util.Log.d("RasFocus", "Blocking $packageName — Reason: $reason")
        // Show block overlay or navigate back
        performGlobalAction(GLOBAL_ACTION_HOME)
    }
}
