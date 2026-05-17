package com.rasel.RasFocus.selfcontrol

// ============================================================
//  RasFocus+ — Self Control Module
//  Mode    : SELF_CONTROL (🧘 Orange)
//  Purpose : Personal focus, Pomodoro timer, distraction blocker
//  Author  : RasFocus+ Architecture Team
// ============================================================

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.rasel.RasFocus.*
import kotlinx.coroutines.delay
import kotlin.math.*

// ──────────────────────────────────────────────────────────────
// Self Control — Accent color override (Orange)
// ──────────────────────────────────────────────────────────────
private val SelfAccent   = RasFocusColors.SelfOrange
private val SelfAccentBg = RasFocusColors.SelfOrange.copy(alpha = 0.08f)

// ============================================================
// SELF CONTROL DASHBOARD SCREEN
// ============================================================

@Composable
fun SelfControlDashboardScreen(viewModel: MainViewModel) {
    val pomodoroState by viewModel.pomodoroState.collectAsState()
    val dailyStats    by viewModel.dailyStats.collectAsState()

    // Pomodoro tick
    LaunchedEffect(pomodoroState.isRunning, pomodoroState.isPaused) {
        while (pomodoroState.isRunning && !pomodoroState.isPaused) {
            delay(1000L)
            viewModel.tickPomodoro()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RasFocusColors.BackgroundWhite)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ──────────────────────────────────────────
        item {
            SelfControlHeader()
        }

        // ── Today's Quick Stats Banner ───────────────────────
        item {
            SelfTodayBanner(stats = dailyStats)
        }

        // ── Pomodoro Timer ───────────────────────────────────
        item {
            SelfPomodoroCard(state = pomodoroState, viewModel = viewModel)
        }

        // ── Weekly Focus Chart ───────────────────────────────
        item {
            SelfWeeklyChartCard(stats = dailyStats)
        }

        // ── Focus Blocklist ──────────────────────────────────
        item {
            SelfBlocklistCard()
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

// ──────────────────────────────────────────────────────────────
// Header
// ──────────────────────────────────────────────────────────────

@Composable
private fun SelfControlHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "My Focus",
                style = MaterialTheme.typography.displayMedium,
                color = RasFocusColors.OnBackground
            )
            Text(
                "Stay disciplined today 🔥",
                style = MaterialTheme.typography.bodyMedium,
                color = RasFocusColors.SubtleText
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(SelfAccent.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🧘", fontSize = 22.sp)
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Today's Quick Stats Banner (3 chips)
// ──────────────────────────────────────────────────────────────

@Composable
private fun SelfTodayBanner(stats: List<DailyStats>) {
    val today = stats.lastOrNull()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SelfAccentBg, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        SelfStatChip(icon = "⏱", value = "${today?.focusMinutes ?: 0}m", label = "Today")
        SelfStatChip(icon = "🔥", value = "5", label = "Streak")
        SelfStatChip(icon = "🎯", value = "${today?.distractionBlocks ?: 0}", label = "Blocked")
    }
}

@Composable
private fun SelfStatChip(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = SelfAccent
        )
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp), color = RasFocusColors.SubtleText)
    }
}

// ──────────────────────────────────────────────────────────────
// Pomodoro Timer Card (Orange-accented)
// ──────────────────────────────────────────────────────────────

@Composable
private fun SelfPomodoroCard(state: PomodoroState, viewModel: MainViewModel) {
    val totalSeconds = when (state.currentPhase) {
        PomodoroPhase.FOCUS      -> state.focusDurationMinutes * 60
        PomodoroPhase.BREAK      -> state.breakDurationMinutes * 60
        PomodoroPhase.LONG_BREAK -> state.longBreakDurationMinutes * 60
    }
    val progress = state.remainingSeconds.toFloat() / totalSeconds.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "self_pomodoro_progress"
    )
    val phaseColor by animateColorAsState(
        targetValue   = state.currentPhase.color,
        animationSpec = tween(600),
        label         = "self_phase_color"
    )
    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60

    val pulseInfinite = rememberInfiniteTransition(label = "self_pulse")
    val pulseScale by pulseInfinite.animateFloat(
        initialValue  = 1f,
        targetValue   = if (state.isRunning) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            tween(if (state.isRunning) 1200 else 1, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "self_pulse_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 8.dp,
                shape        = RasFocusShapes.Card,
                spotColor    = SelfAccent.copy(alpha = 0.2f),
                ambientColor = SelfAccent.copy(alpha = 0.1f)
            ),
        shape  = RasFocusShapes.Card,
        colors = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phase chip
            Row(
                modifier = Modifier
                    .background(phaseColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(phaseColor, CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(state.currentPhase.label, style = MaterialTheme.typography.titleMedium, color = phaseColor)
            }

            Spacer(Modifier.height(20.dp))

            // Ring Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = phaseColor.copy(alpha = 0.05f), radius = size.minDimension / 2f)
                    drawArc(
                        color = RasFocusColors.DividerColor, startAngle = -90f, sweepAngle = 360f,
                        useCenter = false, style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(phaseColor.copy(alpha = 0.5f), phaseColor),
                            center = Offset(size.width / 2f, size.height / 2f)
                        ),
                        startAngle = -90f, sweepAngle = animatedProgress * 360f,
                        useCenter = false, style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                    val angle = Math.toRadians((-90 + animatedProgress * 360).toDouble())
                    val dotX  = (size.width / 2 + (size.minDimension / 2 - 8.dp.toPx()) * cos(angle)).toFloat()
                    val dotY  = (size.height / 2 + (size.minDimension / 2 - 8.dp.toPx()) * sin(angle)).toFloat()
                    drawCircle(color = phaseColor, radius = 8.dp.toPx(), center = Offset(dotX, dotY))
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(dotX, dotY))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "%02d:%02d".format(minutes, seconds),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize   = 44.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = phaseColor
                    )
                    Text(
                        "${state.completedRounds} / ${state.totalRounds} rounds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RasFocusColors.SubtleText
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Round dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(state.totalRounds) { i ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (i < state.completedRounds) phaseColor else RasFocusColors.DividerColor,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = { viewModel.resetPomodoro() },
                    modifier = Modifier.size(52.dp),
                    shape    = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    border   = BorderStroke(1.5.dp, RasFocusColors.DividerColor)
                ) {
                    Icon(Icons.Filled.Refresh, null, tint = RasFocusColors.SubtleText, modifier = Modifier.size(20.dp))
                }
                Button(
                    onClick   = { if (state.isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro() },
                    modifier  = Modifier.weight(1f).height(52.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = phaseColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Crossfade(targetState = state.isRunning, label = "self_play_pause") { isRunning ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                null, modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isRunning) "Pause" else if (state.isPaused) "Resume" else "Start Focus",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick  = {},
                    modifier = Modifier.size(52.dp),
                    shape    = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    border   = BorderStroke(1.5.dp, RasFocusColors.DividerColor)
                ) {
                    Icon(Icons.Filled.SkipNext, null, tint = RasFocusColors.SubtleText, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Weekly Focus Chart (Orange bars)
// ──────────────────────────────────────────────────────────────

@Composable
private fun SelfWeeklyChartCard(stats: List<DailyStats>) {
    val maxMinutes = stats.maxOfOrNull { it.focusMinutes }?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RasFocusShapes.Card, spotColor = SelfAccent.copy(alpha = 0.12f)),
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
                Text("Focus This Week", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${stats.sumOf { it.focusMinutes / 60 }}h ${stats.sumOf { it.focusMinutes % 60 }}m",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SelfAccent
                )
            }
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEachIndexed { index, stat ->
                    val barHeightFraction = stat.focusMinutes / maxMinutes
                    val animatedHeight by animateFloatAsState(
                        targetValue   = barHeightFraction,
                        animationSpec = tween(600, delayMillis = index * 60, easing = FastOutSlowInEasing),
                        label         = "self_bar_$index"
                    )
                    val isToday = index == stats.size - 1

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (stat.focusMinutes > 0) {
                            Text(
                                "${stat.focusMinutes}m",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 9.sp),
                                color = if (isToday) SelfAccent else RasFocusColors.SubtleText
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .fillMaxHeight(animatedHeight)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = if (isToday)
                                            listOf(SelfAccent, SelfAccent.copy(alpha = 0.6f))
                                        else
                                            listOf(SelfAccent.copy(alpha = 0.45f), SelfAccent.copy(alpha = 0.2f))
                                    ),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                stats.forEachIndexed { index, stat ->
                    Text(
                        stat.day,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = if (index == stats.size - 1) SelfAccent else RasFocusColors.SubtleText,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = RasFocusColors.DividerColor)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                SelfSummaryItem("🎯", "${stats.sumOf { it.distractionBlocks }}", "Blocks")
                SelfSummaryItem("🔥", "5", "Day Streak")
                SelfSummaryItem("⏱", "${stats.maxOfOrNull { it.focusMinutes } ?: 0}m", "Best Day")
            }
        }
    }
}

@Composable
private fun SelfSummaryItem(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 22.sp)
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 16.sp), color = SelfAccent)
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp), color = RasFocusColors.SubtleText)
    }
}

// ──────────────────────────────────────────────────────────────
// Focus Blocklist Card (Orange toggles)
// ──────────────────────────────────────────────────────────────

@Composable
private fun SelfBlocklistCard() {
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
            .shadow(4.dp, RasFocusShapes.Card, spotColor = SelfAccent.copy(alpha = 0.1f)),
        shape     = RasFocusShapes.Card,
        colors    = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Focus Blocklist", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Block categories during focus sessions",
                style = MaterialTheme.typography.bodyMedium,
                color = RasFocusColors.SubtleText
            )
            Spacer(Modifier.height(12.dp))

            toggleStates.entries.forEachIndexed { index, entry ->
                val isOn = entry.value
                val animatedBg by animateColorAsState(
                    targetValue = if (isOn) SelfAccent.copy(alpha = 0.07f) else Color.Transparent,
                    animationSpec = tween(300),
                    label = "self_toggle_bg_$index"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(animatedBg, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(icons[entry.key] ?: "📌", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        entry.key,
                        style    = MaterialTheme.typography.titleMedium,
                        color    = if (isOn) RasFocusColors.OnBackground else RasFocusColors.SubtleText,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked         = isOn,
                        onCheckedChange = { toggleStates[entry.key] = it },
                        colors          = SwitchDefaults.colors(
                            checkedTrackColor   = SelfAccent,
                            uncheckedTrackColor = RasFocusColors.DividerColor
                        )
                    )
                }
                if (index < toggleStates.size - 1) {
                    Divider(
                        color     = RasFocusColors.DividerColor.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
