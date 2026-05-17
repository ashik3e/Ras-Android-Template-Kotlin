package com.rasel.RasFocus.parental

// ============================================================
//  RasFocus+ — Parental Control Module
//  Mode    : PARENTAL (👨‍👧 Teal)
//  Purpose : Monitor children's devices, screen time, content filter
//  Author  : RasFocus+ Architecture Team
// ============================================================

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.rasel.RasFocus.*

// ──────────────────────────────────────────────────────────────
// Parental accent — Teal
// ──────────────────────────────────────────────────────────────
private val ParentalAccent   = RasFocusColors.PrimaryTeal
private val ParentalAccentBg = RasFocusColors.PrimaryTeal.copy(alpha = 0.07f)

// ============================================================
// PARENTAL DASHBOARD SCREEN
// ============================================================

@Composable
fun ParentalDashboardScreen(viewModel: MainViewModel) {
    val devices     by viewModel.devices.collectAsState()
    val selectedTab by viewModel.selectedDeviceTab.collectAsState()
    val showPairDialog by viewModel.showPairDialog.collectAsState()

    if (showPairDialog) {
        ParentalPairDeviceDialog(viewModel = viewModel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RasFocusColors.BackgroundWhite)
            .statusBarsPadding()
    ) {
        // ── Header ──────────────────────────────────────────
        ParentalHeader(deviceCount = devices.size, onPairClick = { viewModel.openPairDialog() })

        // ── Summary Strip ───────────────────────────────────
        ParentalSummaryStrip(devices = devices)

        Spacer(Modifier.height(8.dp))

        // ── Device Type Tabs ─────────────────────────────────
        ParentalDeviceTabs(selectedTab = selectedTab, onTabSelect = { viewModel.selectDeviceTab(it) })

        Spacer(Modifier.height(8.dp))

        // ── Device Content ───────────────────────────────────
        Crossfade(
            targetState   = selectedTab,
            animationSpec = tween(350),
            label         = "parental_tab_content"
        ) { tab ->
            when (tab) {
                DeviceType.MOBILE -> {
                    val mobileDevices = devices.filter { it.type == DeviceType.MOBILE }
                    ParentalMobileView(devices = mobileDevices, viewModel = viewModel)
                }
                DeviceType.PC -> {
                    val pcDevices = devices.filter { it.type == DeviceType.PC }
                    ParentalPcView(devices = pcDevices, viewModel = viewModel)
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Header
// ──────────────────────────────────────────────────────────────

@Composable
private fun ParentalHeader(deviceCount: Int, onPairClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Family Control", style = MaterialTheme.typography.displayMedium, color = RasFocusColors.OnBackground)
            Text(
                "$deviceCount device${if (deviceCount != 1) "s" else ""} monitored",
                style = MaterialTheme.typography.bodyMedium,
                color = RasFocusColors.SubtleText
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ParentalAccent.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Notifications, null, tint = ParentalAccent, modifier = Modifier.size(20.dp))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ParentalAccent, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onPairClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Summary Strip — total online, locked, alerts
// ──────────────────────────────────────────────────────────────

@Composable
private fun ParentalSummaryStrip(devices: List<Device>) {
    val online  = devices.count { it.isOnline }
    val locked  = devices.count { it.isLocked }
    val guarded = devices.count { it.isHalalGuardOn }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(ParentalAccentBg, RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ParentalStatChip("🟢", "$online", "Online")
        VerticalDivider(modifier = Modifier.height(32.dp), color = RasFocusColors.DividerColor)
        ParentalStatChip("🔒", "$locked", "Locked")
        VerticalDivider(modifier = Modifier.height(32.dp), color = RasFocusColors.DividerColor)
        ParentalStatChip("🛡", "$guarded", "Guarded")
    }
}

@Composable
private fun ParentalStatChip(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ParentalAccent
        )
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp), color = RasFocusColors.SubtleText)
    }
}

// ──────────────────────────────────────────────────────────────
// Device Type Tabs
// ──────────────────────────────────────────────────────────────

@Composable
private fun ParentalDeviceTabs(selectedTab: DeviceType, onTabSelect: (DeviceType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(RasFocusColors.SurfaceCard, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        DeviceType.values().forEach { type ->
            val isSelected = selectedTab == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        color = if (isSelected) ParentalAccent else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelect(type) }
                    .shadow(
                        elevation = if (isSelected) 4.dp else 0.dp,
                        shape     = RoundedCornerShape(12.dp),
                        spotColor = ParentalAccent.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(if (type == DeviceType.MOBILE) "📱" else "💻", fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (type == DeviceType.MOBILE) "Mobile" else "PC / Laptop",
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
// Mobile Devices View
// ──────────────────────────────────────────────────────────────

@Composable
fun ParentalMobileView(devices: List<Device>, viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (devices.isEmpty()) {
            item { ParentalEmptyCard(deviceType = DeviceType.MOBILE) }
        }
        items(devices, key = { it.id }) { device ->
            ParentalMobileDeviceCard(device = device, viewModel = viewModel)
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun ParentalMobileDeviceCard(device: Device, viewModel: MainViewModel) {
    var expanded by remember { mutableStateOf(true) }
    val screenProgress = device.screenTimeUsedMinutes.toFloat() / device.screenTimeLimitMinutes.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = screenProgress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "parental_screen_progress_${device.id}"
    )
    val progressColor = when {
        screenProgress < 0.6f  -> RasFocusColors.SuccessGreen
        screenProgress < 0.85f -> RasFocusColors.WarningAmber
        else                   -> RasFocusColors.ErrorRed
    }
    val animatedProgressColor by animateColorAsState(
        targetValue = progressColor, animationSpec = tween(500),
        label = "parental_prog_color_${device.id}"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RasFocusShapes.Card, spotColor = ParentalAccent.copy(alpha = 0.1f)),
        shape     = RasFocusShapes.Card,
        colors    = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Device header row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ParentalAccent.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📱", fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(device.name, style = MaterialTheme.typography.titleLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (device.isOnline) RasFocusColors.SuccessGreen else RasFocusColors.SubtleText,
                                    shape = CircleShape
                                )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = if (device.isOnline) "Online · 🔋${device.batteryLevel}%" else "Offline",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RasFocusColors.SubtleText
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        null, tint = RasFocusColors.SubtleText
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(tween(350)) + fadeIn(tween(350)),
                exit    = shrinkVertically(tween(300)) + fadeOut(tween(300))
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Divider(color = RasFocusColors.DividerColor)
                    Spacer(Modifier.height(16.dp))

                    // Screen time circular ring + controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(90.dp)) {
                                drawArc(
                                    color = RasFocusColors.DividerColor, startAngle = -90f, sweepAngle = 360f,
                                    useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = animatedProgressColor, startAngle = -90f, sweepAngle = animatedProgress * 360f,
                                    useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${device.screenTimeUsedMinutes}m",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = animatedProgressColor
                                )
                                Text(
                                    "of ${device.screenTimeLimitMinutes}m",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 9.sp),
                                    color = RasFocusColors.SubtleText
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f).padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Screen Time",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 15.sp),
                                color = RasFocusColors.OnBackground
                            )
                            Text(
                                "${device.screenTimeLimitMinutes - device.screenTimeUsedMinutes}m remaining",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RasFocusColors.SubtleText
                            )
                            // Halal Guard toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (device.isHalalGuardOn)
                                            RasFocusColors.SuccessGreen.copy(alpha = 0.1f)
                                        else
                                            RasFocusColors.DividerColor.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "🛡 Halal Guard",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                                    color = if (device.isHalalGuardOn) RasFocusColors.SuccessGreen else RasFocusColors.SubtleText
                                )
                                Switch(
                                    checked = device.isHalalGuardOn,
                                    onCheckedChange = { viewModel.toggleHalalGuard(device.id) },
                                    modifier = Modifier.scale(0.75f),
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor   = RasFocusColors.SuccessGreen,
                                        uncheckedTrackColor = RasFocusColors.DividerColor
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Lock Button
                    val lockBtnColor by animateColorAsState(
                        targetValue = if (device.isLocked) RasFocusColors.ErrorRed else ParentalAccent,
                        animationSpec = tween(400), label = "parental_lock_color"
                    )
                    Button(
                        onClick  = { viewModel.toggleDeviceLock(device.id) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RasFocusShapes.Button,
                        colors   = ButtonDefaults.buttonColors(containerColor = lockBtnColor),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Icon(
                            if (device.isLocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            null, modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (device.isLocked) "🔓 Unlock Phone" else "🔒 Lock Phone Now",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Blocked apps
                    if (device.blockedAppsList.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Blocked Apps", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        device.blockedAppsList.forEachIndexed { index, app ->
                            var dismissed by remember { mutableStateOf(false) }
                            var offsetX   by remember { mutableFloatStateOf(0f) }
                            AnimatedVisibility(
                                visible = !dismissed,
                                exit    = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .offset(x = offsetX.dp)
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    if (offsetX > 120f) {
                                                        dismissed = true
                                                        viewModel.removeBlockedApp(device.id, app.id)
                                                    } else {
                                                        offsetX = 0f
                                                    }
                                                }
                                            ) { _, dragAmount ->
                                                offsetX = (offsetX + dragAmount / 3f).coerceAtLeast(0f)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(RasFocusColors.BackgroundWhite, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(app.icon, fontSize = 20.sp)
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(app.displayName, style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp))
                                            Text(app.category, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp), color = RasFocusColors.SubtleText)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(RasFocusColors.ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("Blocked", style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp), color = RasFocusColors.ErrorRed)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "← Swipe right to unblock",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                            color = RasFocusColors.SubtleText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// PC Devices View
// ──────────────────────────────────────────────────────────────

@Composable
fun ParentalPcView(devices: List<Device>, viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (devices.isEmpty()) {
            item { ParentalEmptyCard(deviceType = DeviceType.PC) }
        }
        items(devices, key = { it.id }) { device ->
            ParentalPcDeviceCard(device = device, viewModel = viewModel)
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun ParentalPcDeviceCard(device: Device, viewModel: MainViewModel) {
    var showProcesses by remember { mutableStateOf(false) }
    val pulseAnim = rememberInfiniteTransition(label = "parental_live_pulse")
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "parental_pulse_alpha"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RasFocusShapes.Card, spotColor = ParentalAccent.copy(alpha = 0.1f)),
        shape     = RasFocusShapes.Card,
        colors    = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // PC Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ParentalAccent.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💻", fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(device.name, style = MaterialTheme.typography.titleLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(RasFocusColors.SuccessGreen.copy(alpha = pulseAlpha), CircleShape)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Live · ${device.runningProcesses.size} processes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RasFocusColors.SubtleText
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(RasFocusColors.SuccessGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("🔋 ${device.batteryLevel}%", style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = RasFocusColors.SuccessGreen)
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = RasFocusColors.DividerColor)
            Spacer(Modifier.height(16.dp))

            // Suspicious processes
            val suspicious = device.runningProcesses.filter { it.isSuspicious }
            if (suspicious.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RasFocusColors.WarningAmber.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = RasFocusColors.WarningAmber, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${suspicious.size} suspicious process${if (suspicious.size > 1) "es" else ""} detected",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = RasFocusColors.WarningAmber
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Halal Guard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (device.isHalalGuardOn)
                            RasFocusColors.SuccessGreen.copy(alpha = 0.08f)
                        else
                            RasFocusColors.DividerColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "🛡 Force Halal Guard",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (device.isHalalGuardOn) RasFocusColors.SuccessGreen else RasFocusColors.SubtleText
                    )
                    Text(
                        "DNS-based content filtering (VPN)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = RasFocusColors.SubtleText
                    )
                }
                Switch(
                    checked = device.isHalalGuardOn,
                    onCheckedChange = { viewModel.toggleHalalGuard(device.id) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor   = RasFocusColors.SuccessGreen,
                        uncheckedTrackColor = RasFocusColors.DividerColor
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // Emergency Lock Button
            val lockPressed = remember { MutableInteractionSource() }
            val isLockBtnPressed by lockPressed.collectIsPressedAsState()
            val lockBtnScale by animateFloatAsState(
                targetValue = if (isLockBtnPressed) 0.96f else 1f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy),
                label = "parental_lock_scale"
            )

            Button(
                onClick    = { viewModel.toggleDeviceLock(device.id) },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .scale(lockBtnScale),
                shape      = RoundedCornerShape(20.dp),
                colors     = ButtonDefaults.buttonColors(
                    containerColor = if (device.isLocked) RasFocusColors.ErrorRed else RasFocusColors.LockRed
                ),
                elevation  = ButtonDefaults.buttonElevation(8.dp),
                interactionSource = lockPressed
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (device.isLocked) "🔓 UNLOCK PC SCREEN" else "🔒 LOCK PC SCREEN",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp, letterSpacing = 1.sp)
                    )
                    Text(
                        if (device.isLocked) "Click to remotely unlock" else "Emergency — locks immediately",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Processes Expandable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showProcesses = !showProcesses },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Running Processes (${device.runningProcesses.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = RasFocusColors.OnBackground
                )
                Icon(
                    if (showProcesses) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null, tint = RasFocusColors.SubtleText
                )
            }

            AnimatedVisibility(
                visible = showProcesses,
                enter   = expandVertically(tween(350)) + fadeIn(tween(350)),
                exit    = shrinkVertically(tween(300)) + fadeOut(tween(300))
            ) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RasFocusColors.SurfaceCard, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("Process", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                        Text("CPU", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(45.dp), textAlign = TextAlign.End)
                        Text("RAM", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
                    }
                    Spacer(Modifier.height(4.dp))
                    device.runningProcesses.forEach { process ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (process.isSuspicious) RasFocusColors.WarningAmber.copy(alpha = 0.06f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                if (process.isSuspicious) {
                                    Icon(Icons.Filled.Warning, null, tint = RasFocusColors.WarningAmber, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    process.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = if (process.isSuspicious) RasFocusColors.WarningAmber else RasFocusColors.OnSurface,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                "${process.cpuUsage}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = when {
                                    process.cpuUsage > 25f -> RasFocusColors.ErrorRed
                                    process.cpuUsage > 10f -> RasFocusColors.WarningAmber
                                    else                   -> RasFocusColors.SubtleText
                                },
                                modifier = Modifier.width(45.dp), textAlign = TextAlign.End
                            )
                            Text(
                                "${process.memoryMB}MB",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = RasFocusColors.SubtleText,
                                modifier = Modifier.width(50.dp), textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Pair Device Dialog (Parental-themed)
// ──────────────────────────────────────────────────────────────

@Composable
fun ParentalPairDeviceDialog(viewModel: MainViewModel) {
    val pin by viewModel.connectionPin.collectAsState()
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
                        .background(ParentalAccent.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.DevicesOther, null, tint = ParentalAccent, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("Pair Child's Device", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                Text(
                    "Enter this PIN on your child's device",
                    style = MaterialTheme.typography.bodyMedium, color = RasFocusColors.SubtleText,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pinChars.forEachIndexed { index, char ->
                        val delayedAlpha by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(300, delayMillis = index * 80),
                            label = "parental_pin_alpha_$index"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .alpha(delayedAlpha)
                                .background(
                                    brush = Brush.linearGradient(listOf(ParentalAccent, RasFocusColors.PrimaryTealDark)),
                                    shape = RoundedCornerShape(14.dp)
                                ),
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
                        Icon(Icons.Filled.Refresh, null, tint = ParentalAccent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Refresh", color = ParentalAccent, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                listOf(
                    "1️⃣  Install RasFocus+ on child's device",
                    "2️⃣  Open app → Select 'Link to Parent'",
                    "3️⃣  Enter the 6-digit PIN shown above"
                ).forEach { step ->
                    Text(
                        step,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = RasFocusColors.OnSurface,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.closePairDialog() },
                shape   = RasFocusShapes.Button,
                colors  = ButtonDefaults.buttonColors(containerColor = ParentalAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = null
    )
}

// ──────────────────────────────────────────────────────────────
// Empty State Card
// ──────────────────────────────────────────────────────────────

@Composable
private fun ParentalEmptyCard(deviceType: DeviceType) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RasFocusShapes.Card, spotColor = ParentalAccent.copy(alpha = 0.1f)),
        shape  = RasFocusShapes.Card,
        colors = CardDefaults.cardColors(containerColor = RasFocusColors.SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(if (deviceType == DeviceType.MOBILE) "📱" else "💻", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "No ${if (deviceType == DeviceType.MOBILE) "Mobile" else "PC"} Devices",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Tap the + button above to pair\na ${if (deviceType == DeviceType.MOBILE) "mobile device" else "PC or laptop"}",
                style = MaterialTheme.typography.bodyMedium,
                color = RasFocusColors.SubtleText,
                textAlign = TextAlign.Center
            )
        }
    }
}
