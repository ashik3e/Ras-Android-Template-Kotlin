package com.rasel.RasFocus.features

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Coil for loading Favicons from the internet
import coil.compose.AsyncImage

// রিয়েল ডেটাবেস এবং সার্ভিস ইমপোর্ট করা হলো
import com.rasel.RasFocus.DataManager

// ==========================================
// C++ Colors Translation (আপনার থিম কালার)
// ==========================================
val DClrTeal = Color(0xFF0CA8B0)
val DClrWhite = Color(0xFFFFFFFF)
val DClrDark = Color(0xFF323232)
val DClrGrayText = Color(0xFF787878)
val DClrBg = Color(0xFFF8FAFC)
val DClrRed = Color(0xFFE74C3C)
val DClrGreen = Color(0xFF5AAA14)

data class BlockItem(val name: String, var isHoveredCross: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Deep_study() {
    // --- Sub Tab States ---
    var activeSubTab by remember { mutableIntStateOf(0) }

    // --- State Variables ---
    var isFocusMode by remember { mutableStateOf(false) }
    var isBreak by remember { mutableStateOf(false) }
    
    // Timer Settings
    var focusMin by remember { mutableIntStateOf(25) }
    var restMin by remember { mutableIntStateOf(5) }
    var totalSessions by remember { mutableIntStateOf(4) }
    var currentSession by remember { mutableIntStateOf(1) }

    // Toggles
    var chkSound by remember { mutableStateOf(false) }
    var chkFloat by remember { mutableStateOf(false) }
    var chkNet by remember { mutableStateOf(false) }
    var chkSet by remember { mutableStateOf(true) }
    var chkTask by remember { mutableStateOf(true) }
    var chkBlockBreak by remember { mutableStateOf(false) }
    var chkStrict by remember { mutableStateOf(DataManager.isDeepStudyStrict) }
    var chkHideBreakClose by remember { mutableStateOf(false) }

    // Sound Setup
    var soundType by remember { mutableIntStateOf(0) }
    val soundOptions = listOf("White Noise", "Classic Brown", "Deep Brown", "Warm Brown", "Heavy Rain", "Waterfall", "Wind", "Deep Focus", "Space Drone", "Cosmic Brown")

    // Allow Lists Data
    val allowWebs = remember { mutableStateListOf<BlockItem>().apply { addAll(DataManager.userWebList.map { BlockItem(it) }) } }
    val allowApps = remember { mutableStateListOf<BlockItem>().apply { addAll(DataManager.userAppList.map { BlockItem(it) }) } }

    // Bottom Sheet States
    var showBottomSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DClrBg)
        ) {
            // ==========================================
            // 1. TOP TABS
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth().background(DClrWhite).padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val tabs = listOf("Pomodoro", "Active Recall", "Spaced Repetition")
                tabs.forEachIndexed { index, title ->
                    Button(
                        onClick = { activeSubTab = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeSubTab == index) DClrTeal else Color(0xFFE6E6E6),
                            contentColor = if (activeSubTab == index) DClrWhite else DClrDark
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp).height(40.dp)
                    ) {
                        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            // ==========================================
            // MAIN CONTENT (Pomodoro)
            // ==========================================
            if (activeSubTab == 0) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(scrollState)) {

                    // --- TOP TIMER UI ---
                    Card(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isBreak) DClrGreen else DClrTeal),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(if (isFocusMode) "$focusMin:00" else "00:00", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = DClrWhite)
                            Text(if (isBreak) "Break Time!" else "Session $currentSession of $totalSessions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DClrWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- START/STOP BUTTON ---
                    Button(
                        onClick = { 
                            if (!(isFocusMode && chkStrict)) {
                                isFocusMode = !isFocusMode 
                                if (isFocusMode) {
                                    DataManager.isDeepStudyStrict = chkStrict
                                    BlockerAccessibilityService.instance?.startDeepStudySession(focusMin, chkSound)
                                } else {
                                    DataManager.isDeepStudyStrict = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFocusMode && chkStrict) Color(0xFFC81E1E) else if (isFocusMode) DClrRed else DClrTeal
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(55.dp)
                    ) {
                        Text(
                            text = if (isFocusMode && chkStrict) "STRICT MODE: LOCKED" else if (isFocusMode) "STOP POMODORO" else "START POMODORO",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DClrWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- CUSTOM SESSION SETUP ---
                    Text("Custom Session Setup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DClrDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TimerSetupRow("Focus (Minutes):", focusMin, 5, 120, 5, !isFocusMode) { focusMin = it }
                    TimerSetupRow("Rest (Minutes):", restMin, 1, 30, 1, !isFocusMode) { restMin = it }
                    TimerSetupRow("Total Sessions:", totalSessions, 1, 10, 1, !isFocusMode) { totalSessions = it }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- TOGGLES & SOUND ---
                    Text("Settings & Blocking", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DClrDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = chkSound, onCheckedChange = { chkSound = it }, enabled = !(isFocusMode && chkStrict), colors = CheckboxDefaults.colors(checkedColor = DClrTeal))
                        Text("Ambient Sound", fontSize = 15.sp, color = DClrDark, modifier = Modifier.weight(1f))
                        
                        var expandedSound by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.width(140.dp)) {
                            ExposedDropdownMenuBox(expanded = expandedSound, onExpandedChange = { if(chkSound) expandedSound = it }) {
                                OutlinedTextField(
                                    value = soundOptions[soundType], onValueChange = {}, readOnly = true, enabled = chkSound,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSound) },
                                    modifier = Modifier.menuAnchor().height(50.dp),
                                    shape = RoundedCornerShape(8.dp), textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )
                                ExposedDropdownMenu(expanded = expandedSound, onDismissRequest = { expandedSound = false }) {
                                    soundOptions.forEachIndexed { index, option ->
                                        DropdownMenuItem(text = { Text(option, fontSize = 12.sp) }, onClick = { soundType = index; expandedSound = false })
                                    }
                                }
                            }
                        }
                    }

                    DeepCheckbox("Floating Stopwatch", chkFloat, !(isFocusMode && chkStrict)) { chkFloat = it }
                    DeepCheckbox("Block Internet", chkNet, !(isFocusMode && chkStrict)) { chkNet = it }
                    DeepCheckbox("Block Settings / AppData", chkSet, !(isFocusMode && chkStrict)) { chkSet = it }
                    DeepCheckbox("Block Task Manager", chkTask, !(isFocusMode && chkStrict)) { chkTask = it }
                    DeepCheckbox("Keep Blocking During Break", chkBlockBreak, !(isFocusMode && chkStrict)) { chkBlockBreak = it }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Checkbox(
                            checked = chkStrict, 
                            onCheckedChange = { chkStrict = it; DataManager.isDeepStudyStrict = it }, 
                            enabled = !(isFocusMode && chkStrict), 
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFC81E1E))
                        )
                        Text("STRICT MODE (Allow List Only)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if(chkStrict) Color(0xFFC81E1E) else DClrDark)
                    }
                    
                    DeepCheckbox("Hide Close Btn in Break", chkHideBreakClose, !(isFocusMode && chkStrict)) { chkHideBreakClose = it }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    // ==========================================
                    // NEW ALLOW LIST SECTION (Triggers BottomSheet)
                    // ==========================================
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Allowed Apps & Sites", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DClrDark)
                        Text("${allowApps.size + allowWebs.size} Items", fontSize = 14.sp, color = DClrGrayText)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DClrTeal, contentColor = DClrWhite),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !(isFocusMode && chkStrict)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Allow List", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
            // Other Tabs...
            else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tools will be added here...", color = DClrGrayText)
                }
            }
        }

        // ==========================================
        // SMART MODAL BOTTOM SHEET
        // ==========================================
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                modifier = Modifier.fillMaxHeight(0.9f),
                containerColor = DClrBg
            ) {
                BlocklistPickerSheet(
                    onClose = { showBottomSheet = false },
                    onSave = { selectedApps, selectedSites ->
                        // সেভ বাটনে ক্লিক করলে ডাটাবেস আপডেট হবে
                        allowApps.clear()
                        allowApps.addAll(selectedApps.map { BlockItem(it) })
                        DataManager.userAppList = selectedApps

                        allowWebs.clear()
                        allowWebs.addAll(selectedSites.map { BlockItem(it) })
                        DataManager.userWebList = selectedSites

                        showBottomSheet = false
                    },
                    initialApps = allowApps.map { it.name },
                    initialSites = allowWebs.map { it.name }
                )
            }
        }
    }
}

// ==========================================
// BOTTOM SHEET CONTENT COMPOSABLE
// ==========================================
@Composable
fun BlocklistPickerSheet(
    onClose: () -> Unit,
    onSave: (List<String>, List<String>) -> Unit,
    initialApps: List<String>,
    initialSites: List<String>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(1) } // 0=Apps, 1=Sites, 2=Keywords
    
    // লোকাল সিলেকশন স্টেট (যাতে সেভ করার আগে মূল ডাটা চেঞ্জ না হয়)
    val tempSelectedApps = remember { mutableStateListOf<String>().apply { addAll(initialApps) } }
    val tempSelectedSites = remember { mutableStateListOf<String>().apply { addAll(initialSites) } }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        
        // 1. Header with Close Button
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Blocklist / Allowlist", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DClrDark)
            IconButton(onClick = onClose, modifier = Modifier.background(Color(0xFFE5E7EB), CircleShape).size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = DClrDark, modifier = Modifier.size(18.dp))
            }
        }

        // 2. Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search / Add Website", color = DClrGrayText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DClrGrayText) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DClrWhite, unfocusedContainerColor = DClrWhite,
                focusedBorderColor = DClrTeal, unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // 3. Segmented Tabs
        Row(modifier = Modifier.fillMaxWidth().background(DClrWhite, RoundedCornerShape(12.dp)).padding(4.dp)) {
            val tabs = listOf("Apps", "Sites", "Keywords")
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier.weight(1f).height(40.dp)
                        .background(if (selectedTab == index) DClrTeal else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { selectedTab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(title, color = if (selectedTab == index) DClrWhite else DClrDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // 4. Dynamic List Content
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp).background(DClrWhite, RoundedCornerShape(16.dp)).padding(8.dp)) {
            
            // Site Tab Logic: Auto Add ".com" if not existing
            if (selectedTab == 1 && searchQuery.isNotEmpty()) {
                val isExisting = tempSelectedSites.any { it.contains(searchQuery, ignoreCase = true) }
                if (!isExisting) {
                    val suggestedUrl = if (searchQuery.contains(".")) searchQuery else "$searchQuery.com"
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE5E7EB)), contentAlignment = Alignment.Center) {
                                Text(suggestedUrl.first().uppercase(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DClrDark)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(suggestedUrl, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), color = DClrDark)
                            Button(
                                onClick = { 
                                    tempSelectedSites.add(suggestedUrl)
                                    searchQuery = "" 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DClrTeal),
                                shape = RoundedCornerShape(20.dp), modifier = Modifier.height(36.dp)
                            ) { Text("Add", fontSize = 12.sp) }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }

            // Display Selected Sites (Dummy structure for now, shows what's in the list)
            if (selectedTab == 1) {
                items(tempSelectedSites.size) { index ->
                    val site = tempSelectedSites[index]
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Favicon from Google!
                        AsyncImage(
                            model = "https://www.google.com/s2/favicons?domain=${site}&sz=128",
                            contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(site, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DClrDark)
                            Text("Spent: 00 sec | 0 visits", color = DClrGrayText, fontSize = 12.sp)
                        }
                        Checkbox(
                            checked = true, 
                            onCheckedChange = { tempSelectedSites.remove(site) },
                            colors = CheckboxDefaults.colors(checkedColor = DClrTeal)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
            
            // Display Selected Apps
            if (selectedTab == 0) {
                items(tempSelectedApps.size) { index ->
                    val app = tempSelectedApps[index]
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Placeholder App Icon
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DClrTeal.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = DClrTeal)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DClrDark)
                            Text("Spent: 00 sec | 0 launches", color = DClrGrayText, fontSize = 12.sp)
                        }
                        Checkbox(
                            checked = true, 
                            onCheckedChange = { tempSelectedApps.remove(app) },
                            colors = CheckboxDefaults.colors(checkedColor = DClrTeal)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }

        // 5. Sticky Save Button
        Button(
            onClick = { onSave(tempSelectedApps, tempSelectedSites) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DClrTeal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DClrWhite)
        }
    }
}

// --- Helper Composable for Checkboxes ---
@Composable
fun DeepCheckbox(title: String, checked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, colors = CheckboxDefaults.colors(checkedColor = DClrTeal))
        Text(title, fontSize = 15.sp, color = if(enabled) DClrDark else DClrGrayText, modifier = Modifier.clickable(enabled = enabled) { onCheckedChange(!checked) })
    }
}

// --- Helper Composable for +/- Setup ---
@Composable
fun TimerSetupRow(label: String, value: Int, minVal: Int, maxVal: Int, step: Int, enabled: Boolean, onValueChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 15.sp, color = DClrDark, modifier = Modifier.weight(1f))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (enabled && value > minVal) onValueChange(value - step) }, modifier = Modifier.background(Color.White, RoundedCornerShape(4.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)).size(35.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Minus", tint = DClrDark)
            }
            Text("$value", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
            IconButton(onClick = { if (enabled && value < maxVal) onValueChange(value + step) }, modifier = Modifier.background(Color.White, RoundedCornerShape(4.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)).size(35.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Plus", tint = DClrDark)
            }
        }
    }
}
