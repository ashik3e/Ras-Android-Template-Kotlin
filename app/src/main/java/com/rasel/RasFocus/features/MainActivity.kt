package com.rasel.RasFocus.features

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
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

// ==========================================
// Theme Colors for UI
// ==========================================
private val ColTeal = Color(0xFF0CA8B0)
private val ColTealLight = Color(0xFFE0F2F1)
private val ColBgContent = Color(0xFFF1F5F9)
private val ColTextDark = Color(0xFF1E293B)
private val ColGradientStart = Color(0xFF0CA8B0)
private val ColGradientEnd = Color(0xFF007B83)

// ==========================================
// 1. MAIN ACTIVITY CLASS
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DataManager.init(this)
        setContent {
            MaterialTheme {
                AppRootNavigation()
            }
        }
    }
}

// ==========================================
// NOTE: BootReceiver is defined in BootReceiver.kt
// Do NOT declare it here again to avoid Redeclaration error.
// ==========================================

// ==========================================
// 2. ROOT NAVIGATION LOGIC
// ==========================================
@Composable
fun AppRootNavigation() {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(areAllPermissionsGranted(context)) }

    if (permissionsGranted) {
        RasFocusMainContent()
    } else {
        PermissionsPage(onAllGranted = { permissionsGranted = true })
    }
}

// ==========================================
// 3. PERMISSIONS PAGE
// ==========================================
@Composable
fun PermissionsPage(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    var accessibilityGranted by remember { mutableStateOf(false) }
    var overlayGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            accessibilityGranted = isAccessibilityServiceEnabled(context)
            overlayGranted = AndroidSettings.canDrawOverlays(context)
            if (accessibilityGranted && overlayGranted) {
                onAllGranted()
                break
            }
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColBgContent)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Required Permissions", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            "Enable these to start blocking distractions.",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        PermissionCard("Accessibility Service", "To detect app opening", accessibilityGranted) {
            context.startActivity(Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        PermissionCard("Display Over Apps", "To show lock screen", overlayGranted) {
            context.startActivity(Intent(AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text("Wait... Checking status automatically...", fontSize = 12.sp, color = ColTeal)
    }
}

@Composable
fun PermissionCard(title: String, desc: String, granted: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { if (!granted) onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (granted) Icons.Default.CheckCircle else Icons.Default.Settings,
                null,
                tint = if (granted) Color(0xFF10B981) else Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = ColTextDark)
                Text(desc, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// ==========================================
// 4. MAIN APP LAYOUT WITH DRAWER
// ==========================================
@Composable
fun RasFocusMainContent() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }
    BackHandler(enabled = drawerState.isClosed) { (context as? Activity)?.moveTaskToBack(true) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "dashboard"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RasFocusSidebar(currentRoute) { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
                scope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold { padding ->
            NavHost(
                navController,
                "dashboard",
                Modifier.padding(bottom = padding.calculateBottomPadding())
            ) {
                composable("dashboard") {
                    HomeMainScreen(navController) { scope.launch { drawerState.open() } }
                }
                composable("blocks") {
                    Blocks()
                }
                composable("adult_block") {
                    Adult_block()
                }
                composable("deep_study") {
                    Deep_study()
                }
                composable("special_feature") {
                    Speacial()
                }
                composable("statistics") {
                    Statistics()
                }
                composable("settings") {
                    Settings()
                }
            }
        }
    }
}

// ==========================================
// 5. SIDEBAR DESIGN
// ==========================================
@Composable
fun RasFocusSidebar(currentRoute: String, onNavigate: (String) -> Unit) {
    ModalDrawerSheet(drawerContainerColor = ColTeal, modifier = Modifier.width(280.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("RasFocus", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Version 1.0 Pro", fontSize = 12.sp, color = Color(0xFFD0F0F0))
            Spacer(modifier = Modifier.height(32.dp))

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
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "No new notifications", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Alerts",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Content
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Welcome", fontSize = 14.sp, color = Color.Gray)
                Text(
                    "Good Morning",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColTextDark
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E1)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Battery Optimisation",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                "Disable to work properly",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F).copy(alpha = 0.8f)
                            )
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Go to Settings", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Disable")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Analytics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColTextDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnalyticsCard(
                        "Screen Time", "05 sec", "-99 percent",
                        Icons.Default.Timer, Modifier.weight(1f)
                    ) {}
                    AnalyticsCard(
                        "App Launches", "1", "-363 launches",
                        Icons.Default.Star, Modifier.weight(1f)
                    ) {}
                }

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
                return
            }
        }

        if (DataManager.is24HourLockActive) {
            if (System.currentTimeMillis() >= DataManager.lock24hEndTime) {
                DataManager.is24HourLockActive = false
                DataManager.isAdultFocusActive = false
            } else {
                DataManager.isAdultFocusActive = true
            }
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            val rootNode = rootInActiveWindow ?: return
            var currentUrl = ""

            if (packageName.contains("chrome") || packageName.contains("browser") ||
                packageName.contains("edge") || packageName.contains("firefox")
            ) {
                currentUrl = extractUrlFromBrowser(rootNode).lowercase()
            }

            val screenText = event.text.joinToString(" ").lowercase()

            if (isDeepStudyActive && DataManager.isDeepStudyStrict) {
                checkDeepStudyBlocking(packageName, currentUrl)
            } else {
                checkAndBlockContent(packageName, currentUrl, screenText)
            }

            rootNode.recycle()
        }
    }

    private fun extractUrlFromBrowser(nodeInfo: AccessibilityNodeInfo?): String {
        if (nodeInfo == null) return ""
        if (nodeInfo.className == "android.widget.EditText") {
            val id = nodeInfo.viewIdResourceName
            if (id != null && (id.contains("url_bar") || id.contains("address_bar"))) {
                return nodeInfo.text?.toString() ?: ""
            }
        }
        for (i in 0 until nodeInfo.childCount) {
            val childNode = nodeInfo.getChild(i)
            val url = extractUrlFromBrowser(childNode)
            childNode?.recycle()
            if (url.isNotEmpty()) return url
        }
        return ""
    }

    private fun checkDeepStudyBlocking(packageName: String, url: String) {
        if (isSystemApp(packageName)) return

        val allowedApps = DataManager.dsAllowAppList
        val allowedWebs = DataManager.dsAllowWebList

        val isAppAllowed = allowedApps.any { packageName.contains(it, ignoreCase = true) }
        val isWebAllowed = url.isNotEmpty() && allowedWebs.any {
            url.contains(it.substringBefore("."), ignoreCase = true)
        }
        val pauseDuringBreak = isDeepStudyBreak && !DataManager.dsKeepBlockingInBreak

        if (!isAppAllowed && !isWebAllowed && !pauseDuringBreak) {
            if (System.currentTimeMillis() - lastBlockTime < 5000) return
            lastBlockTime = System.currentTimeMillis()

            val goBackSuccess = performGlobalAction(GLOBAL_ACTION_BACK)
            if (!goBackSuccess) performGlobalAction(GLOBAL_ACTION_HOME)

            showFullScreenBlockPopup(
                title = "STAY FOCUSED!",
                message = getMotivationalQuote(),
                reasonInfo = "Reason: App/Website is restricted during Deep Study.",
                bgColorHex = "#4A00E0"
            )
        }
    }

    private fun checkAndBlockContent(packageName: String, url: String, screenText: String) {
        var shouldBlockNormal = false
        var isAdultViolation = false
        var blockReason = ""

        if (DataManager.blockSettingsAndUninstall) {
            if (packageName.contains("com.android.settings") || packageName.contains("packageinstaller")) {
                shouldBlockNormal = true
                blockReason = "Settings are blocked!"
            }
        }

        if (DataManager.isAdultFocusActive && !shouldBlockNormal) {
            when {
                dynamicAdultList.any { url.contains(it) || screenText.contains(it) } -> {
                    isAdultViolation = true; blockReason = "Restricted Website / Keyword"
                }
                adultWebsites.any { url.contains(it) || screenText.contains(it.substringBefore(".")) } -> {
                    isAdultViolation = true; blockReason = "Adult Website Detected"
                }
                hardcoreKeywords.any { url.contains(it) || screenText.contains(it) } -> {
                    isAdultViolation = true; blockReason = "Explicit Keyword Detected"
                }
                romanticKeywords.any { url.contains(it) || screenText.contains(it) } -> {
                    isAdultViolation = true; blockReason = "Softcore/Romantic Content Detected"
                }
                (packageName.contains("youtube") && screenText.contains("shorts")) || url.contains("shorts") -> {
                    shouldBlockNormal = true; blockReason = "YouTube Shorts are blocked!"
                }
                (packageName.contains("facebook") && screenText.contains("reels")) || url.contains("reel") -> {
                    shouldBlockNormal = true; blockReason = "Facebook Reels are blocked!"
                }
            }
        }

        if (DataManager.isFocusActive && !shouldBlockNormal && !isAdultViolation && url.isNotEmpty()) {
            for (web in DataManager.userWebList) {
                val coreName = if (web.contains(".")) web.substringBefore(".") else web
                if (coreName.length > 2 && url.contains(coreName)) {
                    shouldBlockNormal = true
                    blockReason = "Website is in your blocklist."
                    break
                }
            }
        }

        if (!shouldBlockNormal && !isAdultViolation) {
            if (DataManager.isFocusActive) {
                if (DataManager.simpleBlockMode == 1) {
                    if (DataManager.userAppList.any { packageName.contains(it) }) {
                        shouldBlockNormal = true
                        blockReason = "App is in your blocklist."
                    }
                } else if (DataManager.simpleBlockMode == 0) {
                    if (!isSystemApp(packageName) && !DataManager.userAppList.any { packageName.contains(it) }) {
                        shouldBlockNormal = true
                        blockReason = "Only allowed apps can run."
                    }
                }
            }
        }

        if (isAdultViolation) {
            triggerAdultBlockAction(packageName, blockReason)
        } else if (shouldBlockNormal) {
            if (System.currentTimeMillis() - lastBlockTime < 5000) return
            lastBlockTime = System.currentTimeMillis()

            performGlobalAction(GLOBAL_ACTION_HOME)

            val mainMsg = if (DataManager.showQuotes) getReligiousQuote() else getMotivationalQuote()
            showFullScreenBlockPopup(
                title = "ACCESS DENIED!",
                message = mainMsg,
                reasonInfo = "Reason: $blockReason",
                bgColorHex = "#0CA8B0"
            )
        }
    }

    private fun triggerAdultBlockAction(packageName: String, reason: String) {
        if (System.currentTimeMillis() - lastBlockTime < 5000) return
        lastBlockTime = System.currentTimeMillis()

        val isBrowser = packageName.contains("chrome") || packageName.contains("browser") ||
                packageName.contains("edge") || packageName.contains("firefox")

        if (isBrowser) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            Thread.sleep(150)
            performGlobalAction(GLOBAL_ACTION_BACK)
        } else {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }

        DataManager.totalBlockedCount++
        DataManager.cleanStreakDays = 0

        showFullScreenBlockPopup(
            title = "ASTAGFIRULLAH!",
            message = getReligiousQuote(),
            reasonInfo = "Reason: $reason",
            bgColorHex = "#F12B2C"
        )
    }

    private fun getReligiousQuote(): String {
        val quotesList = when (DataManager.adultReligion) {
            0 -> if (DataManager.adultLanguage == 0) muslimQuotesBn else muslimQuotesEn
            1 -> if (DataManager.adultLanguage == 0) hinduQuotesBn else hinduQuotesEn
            2 -> if (DataManager.adultLanguage == 0) christianQuotesBn else christianQuotesEn
            else -> if (DataManager.adultLanguage == 0) motivationalQuotesBn else motivationalQuotesEn
        }
        return quotesList[Random.nextInt(quotesList.size)]
    }

    private fun getMotivationalQuote(): String {
        val quoteList = if (DataManager.adultLanguage == 0) motivationalQuotesBn else motivationalQuotesEn
        return quoteList[Random.nextInt(quoteList.size)]
    }

    fun tryStopFocus(inputPassword: String): Boolean {
        if (DataManager.is24HourLockActive) return false
        val prefs = getSharedPreferences("RasFocusData", Context.MODE_PRIVATE)
        val savedPassword = prefs.getString("friendPassword", "1234") ?: "1234"

        return if (DataManager.controlMode == 1) {
            if (inputPassword == savedPassword) {
                DataManager.isAdultFocusActive = false; true
            } else false
        } else {
            DataManager.isAdultFocusActive = false; true
        }
    }

    fun startDeepStudySession(focusMinutes: Int, playSound: Boolean, soundType: Int = 0) {
        val timeMillis = focusMinutes * 60 * 1000L
        resumeDeepStudySession(timeMillis, playSound, soundType)
    }

    private fun resumeDeepStudySession(timeMillis: Long, playSound: Boolean, soundType: Int) {
        isDeepStudyActive = true
        isDeepStudyBreak = false

        recoveryPrefs.edit()
            .putBoolean("isTimerActive", true)
            .putLong("targetEndTime", System.currentTimeMillis() + timeMillis)
            .putInt("sessionType", 0)
            .putBoolean("playSound", playSound)
            .putInt("soundType", soundType)
            .apply()

        if (playSound) playAmbientSound(soundType)
        showFloatingTimer()

        dsTimer?.cancel()
        dsTimer = object : android.os.CountDownTimer(timeMillis, 30) {
            override fun onTick(millisUntilFinished: Long) {
                dsTimeLeftMillis = millisUntilFinished
                updateFloatingTimerText(millisUntilFinished)
                if (millisUntilFinished in 59000..60030) {
                    showFullScreenBlockPopup(
                        "KEEP GOING!", "⏳ Just 1 Minute Remaining!",
                        "Reason: Deep Study Session Alert", "#4A00E0"
                    )
                }
            }

            override fun onFinish() {
                stopAmbientSound()
                removeFloatingTimer()
                isDeepStudyActive = false
                DataManager.isDeepStudyStrict = false
                recoveryPrefs.edit().clear().apply()
                sendBroadcast(Intent("POMODORO_SESSION_UPDATE"))
                updateNotification("Protection is Active", "Monitoring your focus...")
                showSessionCompletePopup()
            }
        }.start()
    }

    private fun startDeepStudyBreak(breakMinutes: Int) {
        isDeepStudyBreak = true
        val timeMillis = breakMinutes * 60 * 1000L
        showBreakScreenOverlay()

        recoveryPrefs.edit()
            .putBoolean("isTimerActive", true)
            .putLong("targetEndTime", System.currentTimeMillis() + timeMillis)
            .putInt("sessionType", 1)
            .apply()

        dsTimer?.cancel()
        dsTimer = object : android.os.CountDownTimer(timeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateNotification("Break Time!", "Enjoy your break. ${(millisUntilFinished / 60000)} mins left.")
            }

            override fun onFinish() {
                removeBreakScreenOverlay()
                isDeepStudyActive = false
                DataManager.isDeepStudyStrict = false
                recoveryPrefs.edit().clear().apply()
                updateNotification("Protection is Active", "Monitoring your focus...")
                showFullScreenBlockPopup(
                    "TIME'S UP!", "🎉 Break Completed! Ready to focus?",
                    "Reason: Deep Study Break Ended", "#0CA8B0"
                )
                sendBroadcast(Intent("POMODORO_SESSION_UPDATE"))
            }
        }.start()
    }

    private fun showSessionCompletePopup() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            if (sessionCompleteView != null) return@post

            windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            val windowParams = android.view.WindowManager.LayoutParams(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                android.graphics.PixelFormat.TRANSLUCENT
            )

            val layout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(android.graphics.Color.parseColor("#E6000000"))
                isClickable = true
                isFocusable = true
            }

            val card = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setPadding(60, 80, 60, 80)
                val shape = android.graphics.drawable.GradientDrawable()
                shape.cornerRadius = 40f
                shape.setColor(android.graphics.Color.WHITE)
                background = shape
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(80, 0, 80, 0) }
            }

            val title = android.widget.TextView(this).apply {
                text = "SESSION COMPLETED! 🎉"
                textSize = 22f
                setTextColor(android.graphics.Color.parseColor("#0CA8B0"))
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 60)
            }

            val btnRest = android.widget.Button(this).apply {
                text = "Take a Rest (${DataManager.dsRestMin}m)"
                setTextColor(android.graphics.Color.WHITE)
                val btnShape = android.graphics.drawable.GradientDrawable()
                btnShape.cornerRadius = 24f
                btnShape.setColor(android.graphics.Color.parseColor("#10B981"))
                background = btnShape
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 140
                ).apply { setMargins(0, 0, 0, 30) }
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        removeSessionCompletePopup()
                        startDeepStudyBreak(DataManager.dsRestMin)
                    }
                    true
                }
            }

            val btnStart = android.widget.Button(this).apply {
                text = "Start Again (${DataManager.dsFocusMin}m)"
                setTextColor(android.graphics.Color.WHITE)
                val btnShape = android.graphics.drawable.GradientDrawable()
                btnShape.cornerRadius = 24f
                btnShape.setColor(android.graphics.Color.parseColor("#0CA8B0"))
                background = btnShape
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 140
                ).apply { setMargins(0, 0, 0, 30) }
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        removeSessionCompletePopup()
                        val soundType = recoveryPrefs.getInt("soundType", 0)
                        val playSound = recoveryPrefs.getBoolean("playSound", false)
                        startDeepStudySession(DataManager.dsFocusMin, playSound, soundType)
                    }
                    true
                }
            }

            val btnClose = android.widget.Button(this).apply {
                text = "Close & Reset"
                setTextColor(android.graphics.Color.WHITE)
                val btnShape = android.graphics.drawable.GradientDrawable()
                btnShape.cornerRadius = 24f
                btnShape.setColor(android.graphics.Color.parseColor("#E74C3C"))
                background = btnShape
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 140
                )
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        removeSessionCompletePopup()
                    }
                    true
                }
            }

            card.addView(title)
            card.addView(btnRest)
            card.addView(btnStart)
            card.addView(btnClose)
            layout.addView(card)
            sessionCompleteView = layout
            try {
                windowManager?.addView(sessionCompleteView, windowParams)
            } catch (e: Exception) {
                android.util.Log.e("RasFocus", "Error showing session complete popup: ${e.message}")
            }
        }
    }

    private fun removeSessionCompletePopup() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            sessionCompleteView?.let {
                try { windowManager?.removeView(it) } catch (e: Exception) {}
            }
            sessionCompleteView = null
        }
    }

    private fun playAmbientSound(soundType: Int) {
        if (isPlayingNoise) return
        isPlayingNoise = true

        val sampleRate = 44100
        val bufferSize = android.media.AudioTrack.getMinBufferSize(
            sampleRate,
            android.media.AudioFormat.CHANNEL_OUT_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = android.media.AudioTrack(
            android.media.AudioManager.STREAM_MUSIC,
            sampleRate,
            android.media.AudioFormat.CHANNEL_OUT_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            android.media.AudioTrack.MODE_STREAM
        )
        audioTrack?.play()

        noiseThread = Thread {
            val buffer = ShortArray(bufferSize)
            val random = java.util.Random()
            var lastOut = 0.0
            var phase = 0.0
            while (isPlayingNoise) {
                for (i in buffer.indices) {
                    val white = (random.nextDouble() * 2 - 1)
                    var output = 0.0
                    when (soundType) {
                        0 -> output = white * 0.1
                        1 -> { lastOut = (lastOut + 0.02 * white) / 1.02; output = lastOut * 3.5 }
                        2 -> { lastOut = (lastOut + 0.01 * white) / 1.01; output = lastOut * 4.5 }
                        3 -> { lastOut = (lastOut + 0.04 * white) / 1.04; output = lastOut * 2.5 }
                        4 -> { lastOut = (lastOut + 0.02 * white) / 1.02; output = lastOut * 3.5 + (if (random.nextDouble() > 0.99) white * 0.3 else 0.0) }
                        5 -> { lastOut = (lastOut + 0.02 * white) / 1.02; output = lastOut * 2.0 + white * 0.05 }
                        6 -> { lastOut = (lastOut + 0.015 * white) / 1.015; phase += 0.0001; val mod = Math.sin(phase) * 0.5 + 0.5; output = lastOut * 3.0 * (0.4 + 0.6 * mod) }
                        7 -> { lastOut = (lastOut + 0.005 * white) / 1.005; output = lastOut * 6.0 }
                        8 -> { lastOut = (lastOut + 0.008 * white) / 1.008; phase += 0.0005; val drone = Math.sin(phase) * 0.15; output = lastOut * 4.0 + drone }
                        9 -> { lastOut = (lastOut + 0.01 * white) / 1.01; phase += 0.0002; val throb = Math.sin(phase) * 0.3; output = lastOut * 3.5 * (0.7 + throb) }
                        else -> { lastOut = (lastOut + 0.02 * white) / 1.02; output = lastOut * 3.5 }
                    }
                    if (output > 1.0) output = 1.0
                    if (output < -1.0) output = -1.0
                    buffer[i] = (output * Short.MAX_VALUE).toInt().toShort()
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
        noiseThread?.start()
    }

    private fun stopAmbientSound() {
        isPlayingNoise = false
        try { noiseThread?.join(500) } catch (e: Exception) {}
        audioTrack?.let {
            if (it.playState == android.media.AudioTrack.PLAYSTATE_PLAYING) it.stop()
            it.release()
        }
        audioTrack = null
    }

    private fun showFloatingTimer() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            if (floatingTimerView != null) return@post
            windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            val windowParams = android.view.WindowManager.LayoutParams(
                android.view.WindowManager.LayoutParams.WRAP_CONTENT,
                android.view.WindowManager.LayoutParams.WRAP_CONTENT,
                android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.START
                x = 100; y = 200
            }

            val layout = android.widget.LinearLayout(this).apply {
                setPadding(40, 20, 40, 20)
                val shape = android.graphics.drawable.GradientDrawable()
                shape.cornerRadius = 30f
                shape.setColor(android.graphics.Color.parseColor("#0CA8B0"))
                background = shape
                setOnTouchListener { _, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            initialX = windowParams.x; initialY = windowParams.y
                            initialTouchX = event.rawX; initialTouchY = event.rawY; true
                        }
                        android.view.MotionEvent.ACTION_MOVE -> {
                            windowParams.x = initialX + (event.rawX - initialTouchX).toInt()
                            windowParams.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager?.updateViewLayout(this, windowParams); true
                        }
                        else -> false
                    }
                }
            }

            timerTextView = android.widget.TextView(this).apply {
                setTextColor(android.graphics.Color.WHITE)
                textSize = 22f
                setTypeface(null, android.graphics.Typeface.BOLD)
                text = "00:00:00"
            }
            layout.addView(timerTextView)
            floatingTimerView = layout
            try {
                windowManager?.addView(floatingTimerView, windowParams)
            } catch (e: Exception) {
                android.util.Log.e("RasFocus", "Error showing floating timer: ${e.message}")
            }
        }
    }

    private fun updateFloatingTimerText(millis: Long) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            val mins = (millis / 1000) / 60
            val secs = (millis / 1000) % 60
            val ms = (millis % 1000) / 10
            val timeString = String.format("%02d:%02d:%02d", mins, secs, ms)
            timerTextView?.text = timeString
            updateNotification("Deep Study Active", "Time remaining: $timeString")
        }
    }

    private fun removeFloatingTimer() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            floatingTimerView?.let {
                try { windowManager?.removeView(it) } catch (e: Exception) {}
            }
            floatingTimerView = null
        }
    }

    private fun showBreakScreenOverlay() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            if (breakScreenView != null) return@post
            windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            val windowParams = android.view.WindowManager.LayoutParams(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                android.graphics.PixelFormat.TRANSLUCENT
            )

            val layout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                val gradient = android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        android.graphics.Color.parseColor("#4A00E0"),
                        android.graphics.Color.parseColor("#8E2DE2")
                    )
                )
                background = gradient
            }

            val titleView = android.widget.TextView(this).apply {
                text = "TAKE A BREAK!"
                textSize = 45f
                setTextColor(android.graphics.Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 30)
                gravity = android.view.Gravity.CENTER
            }
            val subView = android.widget.TextView(this).apply {
                text = "Breathe deep, rest your eyes, and relax your mind."
                textSize = 18f
                setTextColor(android.graphics.Color.parseColor("#E2E8F0"))
                gravity = android.view.Gravity.CENTER
            }
            layout.addView(titleView)
            layout.addView(subView)
            breakScreenView = layout
            try {
                windowManager?.addView(breakScreenView, windowParams)
            } catch (e: Exception) {
                android.util.Log.e("RasFocus", "Error showing break screen: ${e.message}")
            }
        }
    }

    private fun removeBreakScreenOverlay() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            breakScreenView?.let {
                try { windowManager?.removeView(it) } catch (e: Exception) {}
            }
            breakScreenView = null
        }
    }

    private fun showFullScreenBlockPopup(
        title: String,
        message: String,
        reasonInfo: String,
        bgColorHex: String
    ) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            removeFullScreenBlockPopup()
            windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager

            val windowParams = android.view.WindowManager.LayoutParams(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                android.graphics.PixelFormat.TRANSLUCENT
            )

            val layout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(android.graphics.Color.parseColor(bgColorHex))
                setPadding(60, 60, 60, 60)
                isClickable = true
                isFocusable = true
            }

            val iconView = android.widget.TextView(this).apply {
                text = if (bgColorHex == "#F12B2C") "⚠️" else "🛡️"
                textSize = 60f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 20)
            }

            val titleView = android.widget.TextView(this).apply {
                text = title
                textSize = 35f
                setTextColor(android.graphics.Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 30)
            }

            val reasonView = android.widget.TextView(this).apply {
                text = message
                textSize = 22f
                setTextColor(android.graphics.Color.WHITE)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 40)
            }

            val infoView = android.widget.TextView(this).apply {
                text = reasonInfo
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#E2E8F0"))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 80)
                setTypeface(null, android.graphics.Typeface.ITALIC)
            }

            val btnClose = android.widget.Button(this).apply {
                text = "I Understand & Close"
                setTextColor(android.graphics.Color.parseColor(bgColorHex))
                val btnShape = android.graphics.drawable.GradientDrawable()
                btnShape.cornerRadius = 24f
                btnShape.setColor(android.graphics.Color.WHITE)
                background = btnShape
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 150
                )
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        removeFullScreenBlockPopup()
                    }
                    true
                }
            }

            layout.addView(iconView)
            layout.addView(titleView)
            layout.addView(reasonView)
            layout.addView(infoView)
            layout.addView(btnClose)
            fullScreenBlockView = layout
            try {
                windowManager?.addView(fullScreenBlockView, windowParams)
            } catch (e: Exception) {
                android.util.Log.e("RasFocus", "Error showing block popup: ${e.message}")
            }

            handler.postDelayed({ removeFullScreenBlockPopup() }, 3000)
        }
    }

    private fun removeFullScreenBlockPopup() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            fullScreenBlockView?.let {
                try { windowManager?.removeView(it) } catch (e: Exception) {}
            }
            fullScreenBlockView = null
        }
    }

    override fun onInterrupt() {}
}
