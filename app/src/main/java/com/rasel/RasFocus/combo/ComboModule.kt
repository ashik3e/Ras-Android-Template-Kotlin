package com.rasel.RasFocus.combo

// ============================================================
//  RasFocus+ — Pro Combo Module
//  Mode    : COMBO (⚡ Gold)
//  Purpose : Self-control + Family management in one power panel
//  Author  : RasFocus+ Architecture Team
// ============================================================

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.rasel.RasFocus.*
import kotlinx.coroutines.delay
import kotlin.math.*

// ──────────────────────────────────────────────────────────────
// Combo accent — Gold
// ──────────────────────────────────────────────────────────────
private val ComboAccent    = RasFocusColors.ComboGold
private val ComboAccentBg  = RasFocusColors.ComboGold.copy(alpha = 0.08f)
private val ComboGradient  = Brush.linearGradient(
    listOf(RasFocusColors.ComboGold, RasFocusColors.SelfOrange)
)

// Active panel enum
private enum class ComboPanel { MY_FOCUS, FAMILY }

// ============================================================
// COMBO DASHBOARD SCREEN
// ============================================================

@Composable
fun ComboDashboardScreen(viewModel: MainViewModel) {
    val pomodoroState  by viewModel.pomodoroState.collectAsState()
    val dailyStats     by viewModel.dailyStats.collectAsState()
    val devices        by viewModel.devices.collectAsState()
    val showPairDialog by viewModel.showPairDialog.collectAsState()

    var activePanel by remember { mutableStateOf(ComboPanel.MY_FOCUS) }

    // Pomodoro tick (only when MY_FOCUS panel active)
    LaunchedEffect(pomodoroState.isRunning, pomodoroState.isPaused) {
        while (pomodoroState.isRunning && !pomodoroState.isPaused) {
            delay(1000L)
            viewModel.tickPomodoro()
        }
    }

    if (showPairDialog) {
        ComboPairDialog(viewModel = viewModel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RasFocusColors.BackgroundWhite)
            .statusBarsPadding()
    ) {
        // ── Header ──────────────────────────────────────────
        ComboHeader(devices = devices, onAddDevice = { viewModel.openPairDialog() })

        // ── Power Stats Strip ────────────────────────────────
        ComboPowerStrip(pomodoroState = pomodoroState, devices = devices)

        Spacer(Modifier.height(12.dp))

        // ── Panel Toggle ─────────────────────────────────────
        ComboPanelToggle(
            activePanel = activePanel,
            onToggle    = { activePanel = it }
        )

        Spacer(Modifier.height(8.dp))

        // ── Panel Content ─────────────────────────────────────
        Crossfade(
            targetState   = activePanel,
            animationSpec = tween(300),
            label         = "combo_panel_content"
        ) { panel ->
            when (panel) {
                ComboPanel.MY_FOCUS -> ComboFocusPanel(pomodoroState = pomodoroState, stats = dailyStats, viewModel = viewModel)
                ComboPanel.FAMILY   -> ComboFamilyPanel(devices = devices, viewModel = viewModel)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Header (Gold gradient badge)
// ──────────────────────────────────────────────────────────────

@Composable
private fun ComboHeader(devices: List<Device>, onAddDevice: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Pro Combo",
                    style = MaterialTheme.typography.displayMedium,
                    color = RasFocusColors.OnBackground
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(ComboGradient, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "⚡ PRO",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = Color.White
                    )
                }
            }
            Text(
                "${devices.size} devices + personal focus",
                style = MaterialTheme.typography.bodyMedium,
                color = RasFocusColors.SubtleText
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ComboAccent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Notifications, null, tint = ComboAccent, modifier = Modifier.size(20.dp))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(brush = ComboGradient, shape = CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAddDevice() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Power Strip — Focus today + Family quick stats
// ──────────────────────────────────────────────────────────────

@Composable
private fun ComboPowerStrip(pomodoroState: PomodoroState, devices: List<Device>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(ComboAccentBg, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ComboPowerChip("⏱", "${pomodoroState.completedRounds}", "Rounds Today")
        VerticalDivider(modifier = Modifier.height(32.dp), color = RasFocusColors.DividerColor)
        ComboPowerChip("📱", "${devices.count { it.isOnline }}", "Online")
        VerticalDivider(modifier = Modifier.height(32.dp), color = RasFocusColors.DividerColor)
        ComboPowerChip("🔒", "${devices.count { it.isLocked }}", "Locked")
        VerticalDivider(modifier = Modifier.height(32.dp), color = RasFocusColors.DividerColor)
        ComboPowerChip("🛡", "${devices.count { it.isHalalGuardOn }}", "Guarded")
    }
}

@Composable
private fun ComboPowerChip(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 16.sp)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ComboAccent
        )
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 9.sp), color = RasFocusColors.SubtleText)
    }
}

// ──────────────────────────────────────────────────────────────
// Panel Toggle (My Focus / Family)
// ──────────────────────────────────────────────────────────────

@Composable
private fun ComboPanelToggle(activePanel: ComboPanel, onToggle: (ComboPanel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(RasFocusColors.SurfaceCard, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        ComboPanel.values().forEach { panel ->
            val isSelected = activePanel == panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        color = if (isSelected) Color.Transparent else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .then(
                        if (isSelected) Modifier.background(brush = ComboGradient, shape = RoundedCornerShape(12.dp))
                        else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle(panel) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(if (panel == ComboPanel.MY_FOCUS) "🧘" else "👨‍👧", fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (panel == ComboPanel.MY_FOCUS) "My Focus" else "Family",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else RasFocusColors.SubtleText
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Focus Panel (compact Pomodoro + blocklist)
// ──────────────────────────────────────────────────────────────

@Composable
private fun ComboFocusPanel(pomodoroState: PomodoroState, stats: List<DailyStats>, viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ComboCompactPomodoroCard(state = pomodoroState, viewModel = viewModel)
        }
        item {
            ComboWeekBarCard(stats = stats)
        }
        item {
            ComboBlocklistCard()
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ComboCompactPomodoroCard(state: PomodoroState, viewModel: MainViewModel) {
    val totalSeconds = when (state.currentPhase) {
        PomodoroPhase.FOCUS      -> state.focusDurationMinutes * 60
        PomodoroPhase.BREAK      -> state.breakDurationMinutes * 60
        PomodoroPhase.LONG_BREAK -> state.longBreakDurationMinutes * 60
    }
    val progress = state.remainingSeconds.toFloat() / totalSeconds.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "combo_pomodoro_progress"
    )
    val phaseColor by animateColorAsState(
        targetValue   = state.currentPhase.color,
        animationSpec = tween(600),
        label         = "combo_phase_color"
    )
    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RasFocusShapes.Card, spotColor = ComboAccent.copy(alpha = 0.18f)),
        shape     = RasFocusShapes.Card,
        colors    = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = RasFocusColors.DividerColor, startAngle = -90f, sweepAngle = 360f,
                        useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(ComboAccent.copy(alpha = 0.5f), ComboAccent),
                            center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                        ),
                        startAngle = -90f, sweepAngle = animatedProgress * 360f,
                        useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "%02d:%02d".format(minutes, seconds),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = ComboAccent
                    )
                    Text(
                        "${state.completedRounds}/${state.totalRounds}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                        color = RasFocusColors.SubtleText
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Phase chip
                Box(
                    modifier = Modifier
                        .background(phaseColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(state.currentPhase.label, style = MaterialTheme.typography.labelMedium, color = phaseColor)
                }

                Spacer(Modifier.height(12.dp))

                // Play/Pause
                Button(
                    onClick  = { if (state.isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro() },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = ComboAccent),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(
                        if (state.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        null, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (state.isRunning) "Pause" else if (state.isPaused) "Resume" else "Start",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick  = { viewModel.resetPomodoro() },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape    = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp),
                        border   = BorderStroke(1.dp, RasFocusColors.DividerColor)
                    ) {
                        Icon(Icons.Filled.Refresh, null, tint = RasFocusColors.SubtleText, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reset", style = MaterialTheme.typography.labelMedium, color = RasFocusColors.SubtleText)
                    }
                    OutlinedButton(
                        onClick  = {},
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape    = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp),
                        border   = BorderStroke(1.dp, RasFocusColors.DividerColor)
                    ) {
                        Icon(Icons.Filled.SkipNext, null, tint = RasFocusColors.SubtleText, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Skip", style = MaterialTheme.typography.labelMedium, color = RasFocusColors.SubtleText)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComboWeekBarCard(stats: List<DailyStats>) {
    val maxMinutes = stats.maxOfOrNull { it.focusMinutes }?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RasFocusShapes.Card, spotColor = ComboAccent.copy(alpha = 0.1f)),
        shape     = RasFocusShapes.Card,
        colors    = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("This Week", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${stats.sumOf { it.focusMinutes / 60 }}h ${stats.sumOf { it.focusMinutes % 60 }}m",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ComboAccent
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEachIndexed { index, stat ->
                    val animatedHeight by animateFloatAsState(
                        targetValue   = stat.focusMinutes / maxMinutes,
                        animationSpec = tween(600, delayMillis = index * 60),
                        label         = "combo_bar_$index"
                    )
                    val isToday = index == stats.size - 1

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .fillMaxHeight(animatedHeight)
                                .then(
                                    if (isToday) Modifier.background(brush = ComboGradient, shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    else Modifier.background(ComboAccent.copy(alpha = 0.3f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                stats.forEachIndexed { index, stat ->
                    Text(
                        stat.day,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = if (index == stats.size - 1) ComboAccent else RasFocusColors.SubtleText,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ComboBlocklistCard() {
    val toggleStates = remember {
        mutableStateMapOf(
            "Social Media"  to true,
            "Gaming"        to true,
            "Entertainment" to false,
            "News"          to false,
            "Shopping"      to true
        )
    }
    val icons = mapOf(
        "Social Media"  to "📱",
        "Gaming"        to "🎮",
        "Entertainment" to "🎬",
        "News"          to "📰",
        "Shopping"      to "🛒"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RasFocusShapes.Card, spotColor = ComboAccent.copy(alpha = 0.1f)),
        shape     = RasFocusShapes.Card,
        colors    = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Focus Blocklist", style = MaterialTheme.typography.headlineMedium)
            Text("Active during your focus sessions", style = MaterialTheme.typography.bodyMedium, color = RasFocusColors.SubtleText)
            Spacer(Modifier.height(12.dp))
            toggleStates.entries.forEachIndexed { index, entry ->
                val isOn = entry.value
                val animBg by animateColorAsState(
                    targetValue = if (isOn) ComboAccent.copy(alpha = 0.07f) else Color.Transparent,
                    label = "combo_toggle_bg_$index"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(animBg, RoundedCornerShape(12.dp))
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(icons[entry.key] ?: "📌", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        entry.key,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isOn) RasFocusColors.OnBackground else RasFocusColors.SubtleText,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isOn,
                        onCheckedChange = { toggleStates[entry.key] = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = ComboAccent, uncheckedTrackColor = RasFocusColors.DividerColor)
                    )
                }
                if (index < toggleStates.size - 1) {
                    Divider(color = RasFocusColors.DividerColor.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Family Panel (compact device cards)
// ──────────────────────────────────────────────────────────────

@Composable
private fun ComboFamilyPanel(devices: List<Device>, viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (devices.isEmpty()) {
            item { ComboEmptyFamilyCard() }
        }
        items(devices, key = { it.id }) { device ->
            ComboDeviceCard(device = device, viewModel = viewModel)
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ComboDeviceCard(device: Device, viewModel: MainViewModel) {
    val screenProgress = device.screenTimeUsedMinutes.toFloat() / device.screenTimeLimitMinutes.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue   = screenProgress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label         = "combo_device_prog_${device.id}"
    )
    val progressColor = when {
        screenProgress < 0.6f  -> RasFocusColors.SuccessGreen
        screenProgress < 0.85f -> RasFocusColors.WarningAmber
        else                   -> RasFocusColors.ErrorRed
    }
    val animatedProgressColor by animateColorAsState(
        targetValue = progressColor, animationSpec = tween(500),
        label = "combo_prog_color_${device.id}"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .shadow(5.dp, RasFocusShapes.Card, spotColor = ComboAccent.copy(alpha = 0.12f)),
        shape     = RasFocusShapes.Card,
        colors    = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device icon + screen time ring
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(64.dp)) {
                    drawArc(
                        color = RasFocusColors.DividerColor, startAngle = -90f, sweepAngle = 360f,
                        useCenter = false, style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = animatedProgressColor, startAngle = -90f, sweepAngle = animatedProgress * 360f,
                        useCenter = false, style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(if (device.type == DeviceType.MOBILE) "📱" else "💻", fontSize = 22.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(device.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (device.isOnline) RasFocusColors.SuccessGreen else RasFocusColors.SubtleText,
                                CircleShape
                            )
                    )
                }
                Text(
                    "${device.screenTimeUsedMinutes}m / ${device.screenTimeLimitMinutes}m screen time",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = RasFocusColors.SubtleText
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (device.isHalalGuardOn) {
                        ComboMiniChip("🛡", "Guarded", RasFocusColors.SuccessGreen)
                    }
                    if (device.isLocked) {
                        ComboMiniChip("🔒", "Locked", RasFocusColors.ErrorRed)
                    }
                    if (!device.isOnline) {
                        ComboMiniChip("📴", "Offline", RasFocusColors.SubtleText)
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Quick lock toggle
            val lockBtnColor by animateColorAsState(
                targetValue = if (device.isLocked) RasFocusColors.ErrorRed else ComboAccent.copy(alpha = 0.15f),
                label = "combo_quick_lock_${device.id}"
            )
            IconButton(
                onClick  = { viewModel.toggleDeviceLock(device.id) },
                modifier = Modifier
                    .size(40.dp)
                    .background(lockBtnColor, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    if (device.isLocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    null,
                    tint     = if (device.isLocked) Color.White else ComboAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ComboMiniChip(icon: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 10.sp)
        Spacer(Modifier.width(2.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 9.sp), color = color)
    }
}

@Composable
private fun ComboEmptyFamilyCard() {
    Card(
        modifier  = Modifier.fillMaxWidth().shadow(3.dp, RasFocusShapes.Card),
        shape     = RasFocusShapes.Card,
        colors    = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("👨‍👧", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("No Devices Yet", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                "Tap + to pair a child's device",
                style = MaterialTheme.typography.bodyMedium,
                color = RasFocusColors.SubtleText,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Combo Pair Dialog (Gold-themed)
// ──────────────────────────────────────────────────────────────

@Composable
fun ComboPairDialog(viewModel: MainViewModel) {
    val pin      by viewModel.connectionPin.collectAsState()
    val pinChars = pin.chunked(1)

    AlertDialog(
        onDismissRequest = { viewModel.closePairDialog() },
        shape            = RoundedCornerShape(28.dp),
        containerColor   = RasFocusColors.BackgroundWhite,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(brush = ComboGradient, shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.DevicesOther, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("Pair New Device", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                Text(
                    "Enter this PIN on the child's device",
                    style = MaterialTheme.typography.bodyMedium, color = RasFocusColors.SubtleText,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    pinChars.forEachIndexed { index, char ->
                        val delayedAlpha by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(300, delayMillis = index * 80),
                            label = "combo_pin_alpha_$index"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .alpha(delayedAlpha)
                                .background(brush = ComboGradient, shape = RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(char, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, color = Color.White))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PIN expires in 5:00", style = MaterialTheme.typography.bodyMedium, color = RasFocusColors.SubtleText)
                    TextButton(onClick = { viewModel.refreshPin() }) {
                        Icon(Icons.Filled.Refresh, null, tint = ComboAccent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Refresh", color = ComboAccent, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                listOf(
                    "1️⃣  Install RasFocus+ on child's device",
                    "2️⃣  Open app → Select 'Link to Parent'",
                    "3️⃣  Enter the 6-digit PIN shown above"
                ).forEach { step ->
                    Text(step, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = RasFocusColors.OnSurface, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { viewModel.closePairDialog() },
                shape    = RasFocusShapes.Button,
                colors   = ButtonDefaults.buttonColors(containerColor = ComboAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = null
    )
}
