package com.rasel.RasFocus

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ============================================================
// SECTION 1 — DESIGN SYSTEM TOKENS (RESTORED SHAPES)
// ============================================================
object RasFocusColors {
    val PrimaryTeal      = Color(0xFF0096B4)
    val PrimaryTealLight = Color(0xFF4FC3D6)
    val PrimaryTealDark  = Color(0xFF006E85)
    val BackgroundWhite  = Color(0xFFFFFFFF)
    val SurfaceOffWhite  = Color(0xFFF5FFFE)
    val SurfaceCard      = Color(0xFFECFAFD)
    val OnPrimary        = Color(0xFFFFFFFF)
    val OnBackground     = Color(0xFF0A1628)
    val OnSurface        = Color(0xFF1A2B3C)
    val SubtleText       = Color(0xFF6B7F8E)
    val DividerColor     = Color(0xFFD6EEF4)
    val ErrorRed         = Color(0xFFE53935)
    val SuccessGreen     = Color(0xFF43A047)
    val WarningAmber     = Color(0xFFFB8C00)
    val CardShadow       = Color(0xFF0096B4).copy(alpha = 0.12f)
    val LockRed          = Color(0xFFFF3B30)
    val StudentPurple    = Color(0xFF7C4DFF)
    val SelfOrange       = Color(0xFFFF6D00)
    val ComboGold        = Color(0xFFFFAB00)
    val TimerRingBg      = Color(0xFFE0F7FA)
    val BarChartColor    = Color(0xFF0096B4).copy(alpha = 0.75f)
}

// 🔥 FIX 1: RESTORED RasFocusShapes FOR EXTERNAL MODULES 🔥
object RasFocusShapes {
    val BottomSheet   = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Card          = RoundedCornerShape(24.dp)
    val Button        = RoundedCornerShape(16.dp)
    val Pill          = RoundedCornerShape(50.dp)
    val ChipRadius    = 12.dp
    val CardRadius    = 24.dp
}

// ============================================================
// SECTION 2 — DATA MODELS
// ============================================================
enum class UserPersona(
    val displayName: String, val subtitle: String, val icon: String,
    val accentColor: Color, val description: String
) {
    SELF_CONTROL("Self Control", "Only control yourself", "🧘", RasFocusColors.SelfOrange, "Focus on your own productivity."),
    PARENTAL("Parental Control", "Only control your child", "👨‍👧", RasFocusColors.PrimaryTeal, "Monitor and manage child devices."),
    COMBO("Pro Combo", "Self + Parental", "⚡", RasFocusColors.ComboGold, "Full power package for family."),
    STUDENT("Student Mode", "Self + Linked Child", "🎓", RasFocusColors.StudentPurple, "Designed for students with focus timers.")
}

enum class DeviceType { MOBILE, PC }

data class Device(
    val id: String, val name: String, val ownerName: String, val type: DeviceType,
    val isLocked: Boolean = false, val isHalalGuardOn: Boolean = false,
    val screenTimeUsedMinutes: Int = 0, val screenTimeLimitMinutes: Int = 120,
    val blockedAppsList: List<BlockedApp> = emptyList(), val isOnline: Boolean = true,
    val batteryLevel: Int = 75, val runningProcesses: List<RunningProcess> = emptyList()
)
data class BlockedApp(val id: String, val packageName: String, val displayName: String, val category: String, val icon: String)
data class RunningProcess(val pid: Int, val name: String, val cpuUsage: Float, val memoryMB: Int, val isSuspicious: Boolean = false)
data class PermissionItem(val id: String, val title: String, val description: String, val icon: ImageVector, val isRequired: Boolean, var isGranted: Boolean = false)
data class DailyStats(val day: String, val focusMinutes: Int, val distractionBlocks: Int)
data class FocusSession(val durationMinutes: Int, val breakMinutes: Int, val currentRound: Int, val totalRounds: Int)

data class PomodoroState(
    val isRunning: Boolean = false, val isPaused: Boolean = false,
    val currentPhase: PomodoroPhase = PomodoroPhase.FOCUS,
    val remainingSeconds: Int = 25 * 60, val completedRounds: Int = 0,
    val totalRounds: Int = 4, val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5, val longBreakDurationMinutes: Int = 15
)
enum class PomodoroPhase(val label: String, val color: Color) {
    FOCUS("Focus Time", RasFocusColors.PrimaryTeal),
    BREAK("Short Break", RasFocusColors.SuccessGreen),
    LONG_BREAK("Long Break", RasFocusColors.StudentPurple)
}

// ============================================================
// SECTION 3 — NAVIGATION ROUTES
// ============================================================
object Routes {
    const val SPLASH          = "splash"
    const val ROLE_SELECTION  = "role_selection"
    const val MAIN_DASHBOARD  = "main_dashboard"
    const val CHILD_DASHBOARD = "child_dashboard"
}

sealed class BottomNavTab(val route: String, val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object MyFocus : BottomNavTab("my_focus", "My Focus", Icons.Filled.Person, Icons.Outlined.Person)
    object Family : BottomNavTab("family", "Family", Icons.Filled.Home, Icons.Outlined.Home)
    object Settings : BottomNavTab("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    companion object { val all = listOf(MyFocus, Family, Settings) }
}

// ============================================================
// SECTION 4 — MAIN VIEW MODEL 
// ============================================================
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = application.getSharedPreferences("rasfocus_prefs", Context.MODE_PRIVATE)

    fun getSavedPersona(): UserPersona? = prefs.getString("selected_persona", null)?.let { name -> runCatching { UserPersona.valueOf(name) }.getOrNull() }
    private fun savePersona(persona: UserPersona) { prefs.edit().putString("selected_persona", persona.name).apply() }

    private val _selectedPersona = MutableStateFlow<UserPersona?>(null)
    val selectedPersona: StateFlow<UserPersona?> = _selectedPersona.asStateFlow()

    private val _pendingPersona = MutableStateFlow<UserPersona?>(null)
    val pendingPersona: StateFlow<UserPersona?> = _pendingPersona.asStateFlow()

    private val _showPermissionSheet = MutableStateFlow(false)
    val showPermissionSheet: StateFlow<Boolean> = _showPermissionSheet.asStateFlow()

    private val _devices = MutableStateFlow(generateMockDevices())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    private val _selectedDeviceTab = MutableStateFlow(DeviceType.MOBILE)
    val selectedDeviceTab: StateFlow<DeviceType> = _selectedDeviceTab.asStateFlow()

    private val _showPairDialog = MutableStateFlow(false)
    val showPairDialog: StateFlow<Boolean> = _showPairDialog.asStateFlow()

    private val _connectionPin = MutableStateFlow(generatePin())
    val connectionPin: StateFlow<String> = _connectionPin.asStateFlow()

    private val _pomodoroState = MutableStateFlow(PomodoroState())
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()

    private val _dailyStats = MutableStateFlow(generateMockStats())
    val dailyStats: StateFlow<List<DailyStats>> = _dailyStats.asStateFlow()

    private val _studentOtp = MutableStateFlow(List(6) { "" })
    val studentOtp: StateFlow<List<String>> = _studentOtp.asStateFlow()

    private val _otpVerified = MutableStateFlow(false)
    val otpVerified: StateFlow<Boolean> = _otpVerified.asStateFlow()

    fun getPermissionsForPersona(persona: UserPersona): List<PermissionItem> {
        val mediaPermissionId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) "android.permission.READ_MEDIA_IMAGES" else "android.permission.READ_EXTERNAL_STORAGE"
        return listOf(
            PermissionItem("accessibility", "Accessibility Service", "Core engine setup", Icons.Filled.Build, true),
            PermissionItem("usage_stats", "Usage Stats Access", "Tracks package rules", Icons.Filled.List, true),
            PermissionItem(mediaPermissionId, "Media Storage Access", "Required for processing", Icons.Filled.Image, false)
        )
    }

    fun selectPendingPersona(persona: UserPersona) { _pendingPersona.value = persona }
    fun openPermissionSheet() { _showPermissionSheet.value = true }
    fun dismissPermissionSheet() { _showPermissionSheet.value = false }
    fun confirmPersona() {
        val persona = _pendingPersona.value ?: return
        savePersona(persona)
        _selectedPersona.value = persona
        _showPermissionSheet.value = false
    }
    fun restorePersona(persona: UserPersona) { _selectedPersona.value = persona; _pendingPersona.value  = persona }
    fun selectDeviceTab(type: DeviceType) { _selectedDeviceTab.value = type }
    fun openPairDialog() { _connectionPin.value = generatePin(); _showPairDialog.value = true }
    fun closePairDialog() { _showPairDialog.value = false }
    fun refreshPin() { _connectionPin.value = generatePin() }

    fun startPomodoro() { _pomodoroState.value = _pomodoroState.value.copy(isRunning = true, isPaused = false) }
    fun pausePomodoro() { _pomodoroState.value = _pomodoroState.value.copy(isPaused = true, isRunning = false) }
    fun resetPomodoro() { _pomodoroState.value = PomodoroState() }
    fun tickPomodoro() {
        val state = _pomodoroState.value
        if (!state.isRunning || state.isPaused) return
        if (state.remainingSeconds > 0) {
            _pomodoroState.value = state.copy(remainingSeconds = state.remainingSeconds - 1)
        } else {
            val nextRounds = if (state.currentPhase == PomodoroPhase.FOCUS) state.completedRounds + 1 else state.completedRounds
            val nextPhase = when {
                state.currentPhase == PomodoroPhase.FOCUS && nextRounds % state.totalRounds == 0 -> PomodoroPhase.LONG_BREAK
                state.currentPhase == PomodoroPhase.FOCUS -> PomodoroPhase.BREAK
                else -> PomodoroPhase.FOCUS
            }
            val nextDuration = when (nextPhase) {
                PomodoroPhase.FOCUS -> state.focusDurationMinutes * 60
                PomodoroPhase.BREAK -> state.breakDurationMinutes * 60
                PomodoroPhase.LONG_BREAK -> state.longBreakDurationMinutes * 60
            }
            _pomodoroState.value = state.copy(currentPhase = nextPhase, remainingSeconds = nextDuration, completedRounds = nextRounds)
        }
    }

    fun toggleDeviceLock(deviceId: String) { _devices.value = _devices.value.map { if (it.id == deviceId) it.copy(isLocked = !it.isLocked) else it } }
    fun toggleHalalGuard(deviceId: String) { _devices.value = _devices.value.map { if (it.id == deviceId) it.copy(isHalalGuardOn = !it.isHalalGuardOn) else it } }
    fun removeBlockedApp(deviceId: String, appId: String) { _devices.value = _devices.value.map { if (it.id == deviceId) it.copy(blockedAppsList = it.blockedAppsList.filter { app -> app.id != appId }) else it } }
}

private fun generatePin(): String = (100000..999999).random().toString()
private fun generateMockStats(): List<DailyStats> = listOf(DailyStats("Mon", 120, 8), DailyStats("Tue", 95, 12), DailyStats("Wed", 150, 5), DailyStats("Thu", 80, 15), DailyStats("Fri", 175, 3), DailyStats("Sat", 60, 18), DailyStats("Sun", 200, 2))
private fun generateMockDevices(): List<Device> = listOf(Device(id = "dev_001", name = "Rasel's S24", ownerName = "Rasel Jr.", type = DeviceType.MOBILE), Device(id = "dev_002", name = "Rasel's Laptop", ownerName = "Rasel Jr.", type = DeviceType.PC))

// ============================================================
// SECTION 5 — MAIN ACTIVITY
// ============================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = RasFocusColors.BackgroundWhite) {
                    RasFocusApp()
                }
            }
        }
    }
}

@Composable
fun RasFocusApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as Application)
    )

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            LaunchedEffect(Unit) {
                delay(1500)
                val savedPersona = viewModel.getSavedPersona()
                if (savedPersona != null) {
                    viewModel.restorePersona(savedPersona)
                    val dest = if (savedPersona == UserPersona.STUDENT) Routes.CHILD_DASHBOARD else Routes.MAIN_DASHBOARD
                    navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
                } else {
                    navController.navigate(Routes.ROLE_SELECTION) { popUpTo(Routes.SPLASH) { inclusive = true } }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RasFocusColors.PrimaryTeal)
            }
        }
        
        composable(Routes.ROLE_SELECTION) {
            RoleSelectionScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Routes.MAIN_DASHBOARD) {
            MainDashboardScreen(viewModel = viewModel)
        }

        composable(Routes.CHILD_DASHBOARD) {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                try { com.rasel.RasFocus.child.ChildPermissions.startAllServices(context) } catch (e: Exception) {}
            }
            // 🔥 FIX 2: NO TRY-CATCH AROUND COMPOSABLE FUNCTION 🔥
            com.rasel.RasFocus.child.ChildDashboardScreen()
        }
    }
}

// ============================================================
// SECTION 6 — ROLE SELECTION SCREEN (RESTORED)
// ============================================================
@Composable
fun RoleSelectionScreen(navController: NavController, viewModel: MainViewModel) {
    val pendingPersona by viewModel.pendingPersona.collectAsState()
    val selectedPersona by viewModel.selectedPersona.collectAsState()

    LaunchedEffect(selectedPersona) {
        if (selectedPersona != null) {
            val destination = if (selectedPersona == UserPersona.STUDENT) Routes.CHILD_DASHBOARD else Routes.MAIN_DASHBOARD
            navController.navigate(destination) { popUpTo(Routes.ROLE_SELECTION) { inclusive = true } }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(RasFocusColors.BackgroundWhite).systemBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(text = "Welcome to\nRasFocus+", fontSize = 34.sp, fontWeight = FontWeight.Black, color = RasFocusColors.OnBackground)
                Spacer(Modifier.height(8.dp))
                Text(text = "How do you want to use RasFocus+?", fontSize = 15.sp, color = RasFocusColors.SubtleText)
                Spacer(Modifier.height(16.dp))
            }

            itemsIndexed(UserPersona.values()) { _, persona ->
                PersonaCard(
                    persona = persona,
                    isSelected = pendingPersona == persona,
                    onClick = { viewModel.selectPendingPersona(persona) }
                )
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        if (pendingPersona != null) {
            Button(
                onClick = { viewModel.confirmPersona() },
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth().height(56.dp),
                shape = RasFocusShapes.Button,
                colors = ButtonDefaults.buttonColors(containerColor = RasFocusColors.PrimaryTeal)
            ) {
                Text("Continue with ${pendingPersona?.displayName}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PersonaCard(persona: UserPersona, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .shadow(if (isSelected) 8.dp else 2.dp, RasFocusShapes.Card),
        shape = RasFocusShapes.Card,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) persona.accentColor.copy(alpha = 0.1f) else RasFocusColors.SurfaceOffWhite),
        border = BorderStroke(2.dp, if (isSelected) persona.accentColor else Color.Transparent)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).background(persona.accentColor.copy(alpha = 0.12f), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Text(persona.icon, fontSize = 28.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(persona.displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isSelected) persona.accentColor else RasFocusColors.OnBackground)
                Text(persona.subtitle, fontSize = 12.sp, color = persona.accentColor.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text(persona.description, fontSize = 13.sp, color = RasFocusColors.SubtleText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ============================================================
// SECTION 7 — MAIN DASHBOARD ROUTING
// ============================================================
@Composable
fun MainDashboardScreen(viewModel: MainViewModel) {
    val persona by viewModel.selectedPersona.collectAsState()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: BottomNavTab.MyFocus.route

    val isParentOrCombo = persona == UserPersona.PARENTAL || persona == UserPersona.COMBO
    val tabs = if (isParentOrCombo) BottomNavTab.all else BottomNavTab.all.filter { it !is BottomNavTab.Family }

    Scaffold(
        bottomBar = {
            RasFocusBottomBar(
                tabs = tabs,
                currentRoute = currentRoute,
                onTabSelect = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavTab.MyFocus.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavTab.MyFocus.route) {
                when (persona) {
                    UserPersona.SELF_CONTROL -> com.rasel.RasFocus.selfcontrol.SelfControlDashboardScreen(viewModel = viewModel)
                    UserPersona.PARENTAL     -> com.rasel.RasFocus.parental.ParentalDashboardScreen(viewModel = viewModel)
                    UserPersona.COMBO        -> com.rasel.RasFocus.combo.ComboDashboardScreen(viewModel = viewModel)
                    UserPersona.STUDENT      -> com.rasel.RasFocus.selfcontrol.SelfControlDashboardScreen(viewModel = viewModel)
                    null                     -> com.rasel.RasFocus.selfcontrol.SelfControlDashboardScreen(viewModel = viewModel)
                }
            }
            composable(BottomNavTab.Family.route) {
                when (persona) {
                    UserPersona.PARENTAL     -> com.rasel.RasFocus.parental.ParentalDashboardScreen(viewModel = viewModel)
                    UserPersona.COMBO        -> com.rasel.RasFocus.combo.ComboDashboardScreen(viewModel = viewModel)
                    else                     -> Box(Modifier.fillMaxSize()) { Text("Family Tab") }
                }
            }
            composable(BottomNavTab.Settings.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Settings Screen") }
            }
        }
    }
}

@Composable
fun RasFocusBottomBar(tabs: List<BottomNavTab>, currentRoute: String, onTabSelect: (BottomNavTab) -> Unit) {
    NavigationBar(
        containerColor = RasFocusColors.BackgroundWhite,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(6.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            val iconScale by animateFloatAsState(targetValue = if (selected) 1.2f else 1f, label = "")
            NavigationBarItem(
                selected = selected,
                onClick  = { onTabSelect(tab) },
                icon     = { Icon(if (selected) tab.selectedIcon else tab.unselectedIcon, contentDescription = tab.label, modifier = Modifier.scale(iconScale)) },
                label    = { Text(tab.label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                colors   = NavigationBarItemDefaults.colors(selectedIconColor = RasFocusColors.PrimaryTeal, selectedTextColor = RasFocusColors.PrimaryTeal, unselectedIconColor = RasFocusColors.SubtleText, unselectedTextColor = RasFocusColors.SubtleText, indicatorColor = Color.Transparent)
            )
        }
    }
}
