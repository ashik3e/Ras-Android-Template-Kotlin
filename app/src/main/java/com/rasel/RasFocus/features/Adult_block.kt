package com.rasel.RasFocus.features

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.rasel.RasFocus.DataManager

// ==========================================
// C++ Colors Translation
// ==========================================
val AClrTeal = Color(0xFF0CA8B0)
val AClrWhite = Color(0xFFFFFFFF)
val AClrDark = Color(0xFF323232)
val AClrGrayText = Color(0xFF787878)
val AClrBg = Color(0xFFF8FAFC)
val AClrRed = Color(0xFFE74C3C)
val AClrGreen = Color(0xFF5AAA14)
val AClrCardBg = Color(0xFFFAFCFF)

data class AdultCustomItem(val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Adult_block() {
    val context = LocalContext.current

    // --- Sub Tab States ---
    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Safe Browsing, 1 = AI Filter, 2 = Strict Protocols
    
    // --- DataManager Sync States ---
    var isAdultFocusActive by remember { mutableStateOf(DataManager.isAdultFocusActive) }
    var cb24HourLock by remember { mutableStateOf(DataManager.is24HourLockActive) }
    var controlMode by remember { mutableIntStateOf(DataManager.controlMode) } // 0: Self, 1: Friend
    
    var adultReligion by remember { mutableIntStateOf(DataManager.adultReligion) } // 0: Muslim, 1: Hindu, 2: Christian, 3: Universal
    var adultLanguage by remember { mutableIntStateOf(DataManager.adultLanguage) } // 0: Bangla, 1: English
    var cbPeriodicPopups by remember { mutableStateOf(DataManager.isPeriodicPopupsActive) }
    
    // Checkboxes (Local UI states for now, core blocking checks isAdultFocusActive)
    var cbAdultWeb by remember { mutableStateOf(true) }
    var cbHardcore by remember { mutableStateOf(true) }
    var cbRomantic by remember { mutableStateOf(true) }
    var cbFbReels by remember { mutableStateOf(true) }
    var cbYtShorts by remember { mutableStateOf(true) }
    
    // Custom Keywords Sync
    var customInputText by remember { mutableStateOf("") }
    val customAdultKeywords = remember { mutableStateListOf<AdultCustomItem>().apply {
        addAll(DataManager.userCustomAdultKeywords.map { AdultCustomItem(it) })
    } }
    
    // Overlays
    var showTimeOverlay by remember { mutableStateOf(false) }
    var showPassOverlay by remember { mutableStateOf(false) }
    var inputPassText by remember { mutableStateOf("") }
    var isStoppingFocus by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AClrBg)
    ) {
        // ==========================================
        // 1. TOP TABS (Safe Browsing, AI Filter, Strict)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth().background(AClrWhite).padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabs = listOf("Safe Browsing", "AI Filter", "Strict Protocols")
            tabs.forEachIndexed { index, title ->
                Button(
                    onClick = { activeSubTab = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == index) AClrTeal else Color(0xFFE6E6E6),
                        contentColor = if (activeSubTab == index) AClrWhite else AClrDark
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp).height(40.dp)
                ) {
                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        // ==========================================
        // MAIN CONTENT (Safe Browsing - SubTab 0)
        // ==========================================
        if (activeSubTab == 0) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(scrollState)) {
                
                // --- Start Focus Button (Full Width for Mobile) ---
                Button(
                    onClick = {
                        if (cb24HourLock) return@Button
                        if (isAdultFocusActive) {
                            if (controlMode == 1) { 
                                isStoppingFocus = true; showPassOverlay = true 
                            } else { 
                                isAdultFocusActive = false
                                DataManager.isAdultFocusActive = false
                            }
                        } else {
                            if (controlMode == 0) showTimeOverlay = true 
                            else { isStoppingFocus = false; showPassOverlay = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAdultFocusActive) AClrRed else AClrGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    val btnText = if (cb24HourLock && isAdultFocusActive) "Locked (24h)" 
                                  else if (isAdultFocusActive) "Stop Focus" 
                                  else "Start Focus"
                    Text(btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 3 Dropdowns (Mode, Religion, Language) ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdultDropdown("Mode", listOf("Self Control", "Friend Control"), controlMode, !isAdultFocusActive) { 
                        controlMode = it
                        DataManager.controlMode = it
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            AdultDropdown("Religion", listOf("Muslim", "Hindu", "Christian", "Universal"), adultReligion, !isAdultFocusActive) { 
                                adultReligion = it
                                DataManager.adultReligion = it 
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AdultDropdown("Lang", listOf("Bangla", "English"), adultLanguage, !isAdultFocusActive) { 
                                adultLanguage = it
                                DataManager.adultLanguage = it 
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))

                // --- Block Checkboxes ---
                Text("Block Filters", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AClrDark)
                Spacer(modifier = Modifier.height(8.dp))
                AdultCheckbox("Block Adult Websites", cbAdultWeb, !isAdultFocusActive) { cbAdultWeb = it }
                AdultCheckbox("Block Hardcore Keywords", cbHardcore, !isAdultFocusActive) { cbHardcore = it }
                AdultCheckbox("Block Romantic/Softcore", cbRomantic, !isAdultFocusActive) { cbRomantic = it }
                AdultCheckbox("Block Facebook Reels", cbFbReels, !isAdultFocusActive) { cbFbReels = it }
                AdultCheckbox("Block YouTube Shorts", cbYtShorts, !isAdultFocusActive) { cbYtShorts = it }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Custom Keywords ---
                Text("Custom Keywords", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AClrDark)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customInputText, onValueChange = { customInputText = it },
                        placeholder = { Text("e.g. badword") },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), enabled = !isAdultFocusActive
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            if (customInputText.isNotEmpty()) { 
                                customAdultKeywords.add(AdultCustomItem(customInputText))
                                DataManager.userCustomAdultKeywords = customAdultKeywords.map { it.name }
                                customInputText = "" 
                            } 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AClrTeal),
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.height(56.dp), enabled = !isAdultFocusActive
                    ) { Text("+ Add") }
                }
                
                // Custom Keywords List
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.White).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        customAdultKeywords.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(item.name, color = AClrDark)
                                if (!isAdultFocusActive) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = AClrRed, modifier = Modifier.clickable { 
                                        customAdultKeywords.remove(item)
                                        DataManager.userCustomAdultKeywords = customAdultKeywords.map { it.name }
                                    })
                                }
                            }
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))

                // --- Clean Streak Card (Dynamic from DataManager) ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AClrCardBg),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Star", tint = AClrTeal, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Clean Streak", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AClrDark)
                            Text(SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()), fontSize = 12.sp, color = AClrGrayText)
                            Text("${DataManager.cleanStreakDays} Days", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = AClrTeal)
                            Text(if (DataManager.cleanStreakDays > 0) "Keep up the great work!" else "Stay focused to build your streak!", fontSize = 12.sp, color = if(DataManager.cleanStreakDays > 0) AClrGreen else AClrRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Special Options ---
                Text("Special Options", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AClrDark)
                Spacer(modifier = Modifier.height(8.dp))
                
                // 24 Hour Lock Checkbox with Subtext
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = cb24HourLock, 
                        onCheckedChange = { 
                            cb24HourLock = it
                            DataManager.is24HourLockActive = it
                            if (it) {
                                DataManager.lock24hEndTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
                                isAdultFocusActive = true
                                DataManager.isAdultFocusActive = true
                                Toast.makeText(context, "24-Hour Lockdown Activated!", Toast.LENGTH_LONG).show()
                            }
                        }, 
                        enabled = !isAdultFocusActive, 
                        colors = CheckboxDefaults.colors(checkedColor = AClrTeal)
                    )
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text("24-Hour Lockdown Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AClrDark)
                        Text("Force Focus for 24h. Cannot be undone.", fontSize = 12.sp, color = AClrGrayText)
                    }
                }
                
                // Periodic Popups
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = cbPeriodicPopups, 
                        onCheckedChange = { 
                            cbPeriodicPopups = it 
                            DataManager.isPeriodicPopupsActive = it
                        }, 
                        enabled = !isAdultFocusActive, 
                        colors = CheckboxDefaults.colors(checkedColor = AClrTeal)
                    )
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text("Periodic Religious Popups", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AClrDark)
                        Text("Show fullscreen quotes every 25 mins.", fontSize = 12.sp, color = AClrGrayText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Quick Links ---
                Text("Go to AI Filter Settings  →", color = AClrTeal, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { activeSubTab = 1 }.padding(vertical = 8.dp))
                Text("Go to Strict Protocols  →", color = AClrTeal, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { activeSubTab = 2 }.padding(vertical = 8.dp))
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        } 
        else if (activeSubTab == 1) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("AI Filter Design Coming Soon...", color = AClrGrayText)
            }
        }
        else if (activeSubTab == 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Strict Protocols Design Coming Soon...", color = AClrGrayText)
            }
        }
    }

    // ==========================================
    // OVERLAYS (Dialogs)
    // ==========================================
    if (showTimeOverlay) {
        var hours by remember { mutableIntStateOf(1) }
        var mins by remember { mutableIntStateOf(0) }
        
        AlertDialog(
            onDismissRequest = { showTimeOverlay = false },
            title = { Text("SET FOCUS DURATION", fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Hours: $hours", fontWeight = FontWeight.Bold)
                    Slider(value = hours.toFloat(), onValueChange = { hours = it.toInt() }, valueRange = 0f..23f)
                    Text("Mins: $mins", fontWeight = FontWeight.Bold)
                    Slider(value = mins.toFloat(), onValueChange = { mins = it.toInt() }, valueRange = 0f..59f)
                }
            },
            confirmButton = { 
                Button(onClick = { 
                    isAdultFocusActive = true
                    DataManager.isAdultFocusActive = true
                    showTimeOverlay = false 
                }, colors = ButtonDefaults.buttonColors(AClrTeal)) { Text("Start Focus") } 
            },
            dismissButton = { TextButton(onClick = { showTimeOverlay = false }) { Text("Cancel", color = AClrDark) } }
        )
    }

    if (showPassOverlay) {
        AlertDialog(
            onDismissRequest = { showPassOverlay = false },
            title = { Text(if (isStoppingFocus) "ENTER FRIEND'S PASSWORD" else "SET PASSWORD TO START", fontWeight = FontWeight.Bold) },
            text = { OutlinedTextField(value = inputPassText, onValueChange = { inputPassText = it }, placeholder = { Text("Password...") }, singleLine = true) },
            confirmButton = { 
                Button(onClick = { 
                    if (isStoppingFocus) {
                        val success = BlockerAccessibilityService.instance?.tryStopFocus(inputPassText) ?: false
                        if (success) {
                            isAdultFocusActive = false
                            DataManager.isAdultFocusActive = false
                            showPassOverlay = false
                            inputPassText = ""
                        } else {
                            Toast.makeText(context, "Wrong Password or 24h Lock Active!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // স্টার্ট করার সময় (পাসওয়ার্ড '1234' সেট করা আছে সার্ভিসে ডামি হিসেবে)
                        if(inputPassText == "1234") {
                            isAdultFocusActive = true
                            DataManager.isAdultFocusActive = true
                            showPassOverlay = false
                            inputPassText = ""
                        } else {
                            Toast.makeText(context, "Enter valid password to start (1234)", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(AClrTeal)) { Text("Confirm") } 
            },
            dismissButton = { TextButton(onClick = { showPassOverlay = false }) { Text("Cancel", color = AClrDark) } }
        )
    }
}

// --- Helper Composable for Checkboxes ---
@Composable
fun AdultCheckbox(title: String, checked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, colors = CheckboxDefaults.colors(checkedColor = AClrTeal))
        Text(title, fontSize = 15.sp, color = if(enabled) AClrDark else AClrGrayText, modifier = Modifier.clickable(enabled = enabled) { onCheckedChange(!checked) })
    }
}

// --- Helper Composable for Dropdowns ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdultDropdown(label: String, options: List<String>, selectedIndex: Int, enabled: Boolean, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = options[selectedIndex],
            onValueChange = {}, readOnly = true, enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(index); expanded = false })
            }
        }
    }
}
