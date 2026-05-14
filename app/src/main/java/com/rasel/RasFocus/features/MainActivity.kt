package com.example.app

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
<<<<<<< HEAD
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
=======
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import androidx.navigation.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.rasel.RasFocus.DataManager
>>>>>>> a9e692e8e3a2829c657fe9f3d2b314e2ddf124e0

class MainActivity : ComponentActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE)
                    as DevicePolicyManager

        compName = ComponentName(
            this,
            MyDeviceAdminReceiver::class.java
        )

        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {

        // Accessibility
        if (!isAccessibilityEnabled()) {

            showPopup(
                "Accessibility Permission",
                "Please enable accessibility permission"
            ) {

                startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }

            return
        }

        // Overlay Permission
        if (!Settings.canDrawOverlays(this)) {

<<<<<<< HEAD
            showPopup(
                "Display Over Apps",
                "Allow display over other apps"
=======
            SidebarItem("Dashboard", Icons.Default.Dashboard, "dashboard", currentRoute, onNavigate)
            SidebarItem("App Blocks", Icons.Default.Shield, "blocks", currentRoute, onNavigate)
            SidebarItem("Adult Block", Icons.Default.Lock, "adult_block", currentRoute, onNavigate)
            SidebarItem("Deep Study", Icons.Default.MenuBook, "deep_study", currentRoute, onNavigate)
            SidebarItem("Statistics", Icons.Default.Assessment, "statistics", currentRoute, onNavigate)
            SidebarItem("Settings", Icons.Default.Settings, "settings", currentRoute, onNavigate)
        }
    }
}

@Composable
fun SidebarItem(
    label: String,
    icon: ImageVector,
    route: String,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val selected = currentRoute == route
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onNavigate(route) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (selected) ColTeal else Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            label,
            color = if (selected) ColTeal else Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ==========================================
// 6. MAIN DASHBOARD SCREEN
// ==========================================
@Composable
fun HomeMainScreen(navController: NavController, onOpenDrawer: () -> Unit) {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(ColBgContent)) {

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(ColGradientStart, ColGradientEnd)))
                    .statusBarsPadding()
                    .padding(vertical = 24.dp)
>>>>>>> a9e692e8e3a2829c657fe9f3d2b314e2ddf124e0
            ) {

                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )

                startActivity(intent)
            }

            return
        }

        // Media Permission
        if (!hasMediaPermission()) {

            showPopup(
                "Media Permission",
                "Allow media/photos access"
            ) {

                requestMediaPermission.launch(
                    getMediaPermissions()
                )
            }

            return
        }

        // Battery Optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val pm =
                getSystemService(Context.POWER_SERVICE)
                        as PowerManager

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {

                showPopup(
                    "Battery Optimization",
                    "Disable battery optimization"
                ) {

                    val intent = Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )

                    startActivity(intent)
                }

<<<<<<< HEAD
=======
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("deep_study") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E6FA)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFF4B0082)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Take a Break",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF4B0082)
                        )
                    }
                }
            }
        }

        // BOTTOM NAVIGATION BAR
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 12.dp, horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // NOTE: BottomNavItem is defined only in mainScreen.kt
                // Using it here directly (no duplicate declaration in this file)
                BottomNavItem("Dashboard", Icons.Default.Dashboard, selectedBottomTab == 0) {
                    selectedBottomTab = 0
                }
                BottomNavItem("Blocks", Icons.Default.Shield, selectedBottomTab == 1) {
                    selectedBottomTab = 1
                    navController.navigate("blocks")
                }
                BottomNavItem("Study", Icons.Default.MenuBook, selectedBottomTab == 2) {
                    selectedBottomTab = 2
                    navController.navigate("deep_study")
                }
                BottomNavItem("Account", Icons.Default.Person, selectedBottomTab == 3) {
                    selectedBottomTab = 3
                }
            }
        }
    }
}


// ==========================================
// 7. HELPER FUNCTIONS
// ==========================================
fun areAllPermissionsGranted(context: Context): Boolean {
    return isAccessibilityServiceEnabled(context) && AndroidSettings.canDrawOverlays(context)
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedService =
        "${context.packageName}/${context.packageName}.features.BlockerAccessibilityService"
    val enabledServices = AndroidSettings.Secure.getString(
        context.contentResolver,
        AndroidSettings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabledServices?.contains(expectedService) == true
}

// ==========================================
// 8. ACCESSIBILITY SERVICE CLASS (Blocker Engine)
// ==========================================
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

    private val muslimQuotesBn = listOf(
        "মুমিনদের বলুন, তারা যেন তাদের দৃষ্টি নত রাখে...",
        "লজ্জাশীলতা ঈমানের অঙ্গ।"
    )
    private val muslimQuotesEn = listOf(
        "Tell the believing men to reduce their vision...",
        "Modesty is a branch of faith."
    )
    private val hinduQuotesBn = listOf(
        "যে মনকে নিয়ন্ত্রণ করতে পারে, তার মন তার সবচেয়ে বড় বন্ধু।",
        "কাম, ক্রোধ এবং লোভ—এই তিনটি নরকের দ্বার।"
    )
    private val hinduQuotesEn = listOf(
        "For him who has conquered the mind, the mind is the best of friends.",
        "Lust, anger, and greed are the three doors to hell."
    )
    private val christianQuotesBn = listOf(
        "খারাপ সাহচর্য ভালো চরিত্র নষ্ট করে।",
        "অহংকার পতনের মূল।"
    )
    private val christianQuotesEn = listOf(
        "Bad company ruins good morals.",
        "Pride goes before destruction."
    )

    private val motivationalQuotesBn = listOf(
        "সময়ের মূল্য বোঝো, জীবন তোমার মূল্য বুঝবে।",
        "সফলতা আসে ফোকাস থেকে, ডিস্ট্রাকশন থেকে নয়।",
        "আজকের সময় নষ্ট মানে, কালকের স্বপ্ন নষ্ট।",
        "যে নিজের মনকে নিয়ন্ত্রণ করতে পারে, সে পৃথিবী জয় করতে পারে।"
    )
    private val motivationalQuotesEn = listOf(
        "Understand the value of time, life will understand your value.",
        "Success comes from focus, not from distraction.",
        "Wasting time today means ruining tomorrow's dreams.",
        "He who can control his mind can conquer the world."
    )

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
>>>>>>> a9e692e8e3a2829c657fe9f3d2b314e2ddf124e0
                return
            }
        }

        // Device Admin
        if (!devicePolicyManager.isAdminActive(compName)) {

            showPopup(
                "Device Admin",
                "Enable device admin permission"
            ) {

                val intent =
                    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)

                intent.putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    compName
                )

                startActivity(intent)
            }

            return
        }

        // All permissions granted
        openMainInterface()
    }

    private fun openMainInterface() {
        setContentView(R.layout.activity_main)
    }

    private fun showPopup(
        title: String,
        message: String,
        onYes: () -> Unit
    ) {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("YES") { _, _ ->
                onYes()
            }
            .show()
    }

    private fun isAccessibilityEnabled(): Boolean {

        val expected =
            "$packageName/.MyAccessibilityService"

        val enabled =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

        return enabled.contains(expected)
    }

    private fun hasMediaPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= 33) {

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getMediaPermissions(): Array<String> {

        return if (Build.VERSION.SDK_INT >= 33) {

            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )

        } else {

            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private val requestMediaPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            checkPermissions()
        }
}
