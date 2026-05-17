package com.rasel.RasFocus.features

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rasel.RasFocus.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ==========================================
// C++ Colors Translation & Premium Palette
// ==========================================
val SClrTeal = Color(0xFF0CA8B0)
val SClrTealLight = Color(0xFFE0F2F1) // For subtle backgrounds
val SClrWhite = Color(0xFFFFFFFF)
val SClrDark = Color(0xFF1E293B) // Premium dark blue-gray
val SClrGrayText = Color(0xFF64748B)
val SClrBg = Color(0xFFF1F5F9) // Premium soft background
val SClrGreen = Color(0xFF10B981)
val SClrRed = Color(0xFFEF4444)

// ==========================================
// Real-time App List Model
// ==========================================
data class InstalledApp(val name: String, val packageName: String, val icon: Drawable?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Blocks() {
    val context = LocalContext.current
    
    // --- DataManager Sync States ---
    var isFocusActive by remember { mutableStateOf(DataManager.isFocusActive) }
    var controlMode by remember { mutableIntStateOf(DataManager.controlMode) }
    var simpleBlockMode by remember { mutableIntStateOf(DataManager.simpleBlockMode) }
    var showQuotes by remember { mutableStateOf(DataManager.showQuotes) }
    
    // Lists Sync with DataManager
    val webList = remember { mutableStateListOf<String>().apply { addAll(DataManager.userWebList) } }
    val appList = remember { mutableStateListOf<String>().apply { addAll(DataManager.userAppList) } }

    // UI States
    var currentBlockTab by remember { mutableIntStateOf(0) }
    var showTimeOverlay by remember { mutableStateOf(false) }
    var showPassOverlay by remember { mutableStateOf(false) }
    var inputPassText by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(SClrBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // ==========================================
            // 1. PREMIUM ANIMATED TOP TABS
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SClrWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf("Simple", "Schedule", "Device")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = currentBlockTab == index
                        val bgColor by animateColorAsState(if (isSelected) SClrTeal else Color.Transparent, tween(300))
                        val txtColor by animateColorAsState(if (isSelected) SClrWhite else SClrGrayText, tween(300))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .clickable { currentBlockTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(title, color = txtColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            if (currentBlockTab == 0) {
                Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(scrollState)) {
                    
                    // ==========================================
                    // 2. MAIN CONTROLS CARD
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SClrWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            
                            // Control Mode Dropdown (Premium Pill Shape)
                            Text("Control Type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SClrGrayText)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            var expandedControl by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = expandedControl, onExpandedChange = { if(!isFocusActive) expandedControl = it }) {
                                OutlinedTextField(
                                    value = if (controlMode == 0) "Self Control" else "Friend Control",
                                    onValueChange = {}, readOnly = true, enabled = !isFocusActive,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedControl) },
                                    leadingIcon = { Icon(Icons.Rounded.Security, contentDescription = null, tint = SClrTeal) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SClrBg, unfocusedContainerColor = SClrBg,
                                        focusedBorderColor = SClrTeal, unfocusedBorderColor = Color.Transparent
                                    )
                                )
                                ExposedDropdownMenu(expanded = expandedControl, onDismissRequest = { expandedControl = false }) {
                                    DropdownMenuItem(text = { Text("Self Control", fontWeight = FontWeight.Medium) }, onClick = { controlMode = 0; DataManager.controlMode = 0; expandedControl = false })
                                    DropdownMenuItem(text = { Text("Friend Control", fontWeight = FontWeight.Medium) }, onClick = { controlMode = 1; DataManager.controlMode = 1; expandedControl = false })
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Select Mode Dropdown
                            Text("Blocking Action", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SClrGrayText)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            var expandedMode by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = expandedMode, onExpandedChange = { if(!isFocusActive) expandedMode = it }) {
                                OutlinedTextField(
                                    value = if (simpleBlockMode == 0) "Allow (White-list)" else "Block (Black-list)",
                                    onValueChange = {}, readOnly = true, enabled = !isFocusActive,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                                    leadingIcon = { Icon(if(simpleBlockMode == 0) Icons.Rounded.CheckCircle else Icons.Rounded.Block, contentDescription = null, tint = if(simpleBlockMode==0) SClrGreen else SClrRed) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SClrBg, unfocusedContainerColor = SClrBg,
                                        focusedBorderColor = SClrTeal, unfocusedBorderColor = Color.Transparent
                                    )
                                )
                                ExposedDropdownMenu(expanded = expandedMode, onDismissRequest = { expandedMode = false }) {
                                    DropdownMenuItem(text = { Text("Allow (White-list)", color = SClrGreen, fontWeight = FontWeight.Bold) }, onClick = { simpleBlockMode = 0; DataManager.simpleBlockMode = 0; expandedMode = false })
                                    DropdownMenuItem(text = { Text("Block (Black-list)", color = SClrRed, fontWeight = FontWeight.Bold) }, onClick = { simpleBlockMode = 1; DataManager.simpleBlockMode = 1; expandedMode = false })
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Show Quotes Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { showQuotes = !showQuotes; DataManager.showQuotes = showQuotes }.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = showQuotes, 
                                    onCheckedChange = null,
                                    colors = SwitchDefaults.colors(checkedThumbColor = SClrWhite, checkedTrackColor = SClrTeal)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Show Quotes when Blocked", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = SClrDark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // ==========================================
                    // 3. GLOWING START BUTTON
                    // ==========================================
                    val btnColor by animateColorAsState(if (isFocusActive) SClrRed else SClrTeal, tween(300))
                    
                    Button(
                        onClick = {
                            if (isFocusActive) {
                                if (controlMode == 1) showPassOverlay = true else {
                                    isFocusActive = false; DataManager.isFocusActive = false
                                }
                            } else {
                                if (controlMode == 0) showTimeOverlay = true else showPassOverlay = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .shadow(
                                elevation = if (isFocusActive) 16.dp else 12.dp, 
                                shape = RoundedCornerShape(24.dp), 
                                spotColor = btnColor, 
                                ambientColor = btnColor
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            if (isFocusActive) Icons.Rounded.Stop else Icons.Rounded.PlayArrow, 
                            contentDescription = null, 
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isFocusActive) "STOP PROTECTION" else "START PROTECTION", 
                            fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(35.dp))

                    // ==========================================
                    // 4. SMART MANAGE LIST CARD
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SClrWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Monitored Items", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SClrDark)
                                    Text("${appList.size} Apps • ${webList.size} Websites", fontSize = 13.sp, color = SClrGrayText)
                                }
                                Box(
                                    modifier = Modifier.size(48.dp).background(SClrTealLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = SClrTeal)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { showBottomSheet = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SClrBg, contentColor = SClrTeal),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isFocusActive
                            ) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Manage")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manage Apps & Websites", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("More features coming soon...", color = SClrGrayText, fontWeight = FontWeight.Medium)
                }
            }
        }

        // ==========================================
        // DIALOGS (Rounded & Modern)
        // ==========================================
        if (showTimeOverlay) {
            AlertDialog(
                onDismissRequest = { showTimeOverlay = false },
                containerColor = SClrWhite,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Activate Focus", fontWeight = FontWeight.Bold) },
                text = { Text("Are you ready to start the protection mode?", color = SClrGrayText) },
                confirmButton = { 
                    Button(onClick = { isFocusActive = true; DataManager.isFocusActive = true; showTimeOverlay = false }, colors = ButtonDefaults.buttonColors(SClrTeal)) { Text("Start") } 
                },
                dismissButton = { TextButton(onClick = { showTimeOverlay = false }, colors = ButtonDefaults.textButtonColors(contentColor = SClrGrayText)) { Text("Cancel") } }
            )
        }

        if (showPassOverlay) {
            AlertDialog(
                onDismissRequest = { showPassOverlay = false },
                containerColor = SClrWhite,
                shape = RoundedCornerShape(24.dp),
                title = { Text(if (isFocusActive) "Stop Protection" else "Start Protection", fontWeight = FontWeight.Bold) },
                text = { 
                    OutlinedTextField(
                        value = inputPassText, onValueChange = { inputPassText = it }, 
                        placeholder = { Text("Enter 4-digit Password") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SClrTeal)
                    ) 
                },
                confirmButton = { 
                    Button(onClick = { 
                        if(inputPassText == "1234") {
                            isFocusActive = !isFocusActive; DataManager.isFocusActive = isFocusActive
                            showPassOverlay = false; inputPassText = ""
                        }
                    }, colors = ButtonDefaults.buttonColors(SClrTeal)) { Text("Confirm") } 
                }
            )
        }

        // ==========================================
        // 🟢 SMART MODAL BOTTOM SHEET
        // ==========================================
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                modifier = Modifier.fillMaxHeight(0.95f),
                containerColor = SClrBg,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = SClrGrayText.copy(alpha = 0.4f), width = 50.dp) }
            ) {
                BlocklistSmartPicker(
                    context = context,
                    onClose = { showBottomSheet = false },
                    onSave = { selectedApps, selectedSites ->
                        appList.clear(); appList.addAll(selectedApps)
                        DataManager.userAppList = selectedApps

                        webList.clear(); webList.addAll(selectedSites)
                        DataManager.userWebList = selectedSites

                        showBottomSheet = false
                    },
                    initialApps = appList.toList(),
                    initialSites = webList.toList()
                )
            }
        }
    }
}

// ==========================================
// BOTTOM SHEET CONTENT COMPOSABLE
// ==========================================
@Composable
fun BlocklistSmartPicker(
    context: Context,
    onClose: () -> Unit,
    onSave: (List<String>, List<String>) -> Unit,
    initialApps: List<String>,
    initialSites: List<String>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } 
    
    val tempSelectedApps = remember { mutableStateListOf<String>().apply { addAll(initialApps) } }
    val tempSelectedSites = remember { mutableStateListOf<String>().apply { addAll(initialSites) } }

    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val resolveInfoList = pm.queryIntentActivities(intent, 0)
            val apps = resolveInfoList.map {
                InstalledApp(
                    name = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = null 
                )
            }.distinctBy { it.packageName }.sortedBy { it.name }
            installedApps = apps
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Blocklist Manager", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SClrDark)
            IconButton(onClick = onClose, modifier = Modifier.background(SClrWhite, CircleShape).size(36.dp).shadow(2.dp, CircleShape)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = SClrDark, modifier = Modifier.size(20.dp))
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search or add new item...", color = SClrGrayText) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = SClrTeal) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SClrWhite, unfocusedContainerColor = SClrWhite,
                focusedBorderColor = SClrTeal, unfocusedBorderColor = Color.Transparent
            )
        )

        // Segmented Tabs inside Bottom Sheet
        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SClrWhite)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                val tabs = listOf("Apps", "Sites")
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val bgColor by animateColorAsState(if (isSelected) SClrTeal else Color.Transparent, tween(200))
                    val txtColor by animateColorAsState(if (isSelected) SClrWhite else SClrDark, tween(200))
                    
                    Box(
                        modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(8.dp)).background(bgColor).clickable { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(title, color = txtColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp).clip(RoundedCornerShape(20.dp)).background(SClrWhite).padding(8.dp)) {
            
            // Auto Add Sites Logic
            if (selectedTab == 1 && searchQuery.isNotEmpty()) {
                val isExisting = tempSelectedSites.any { it.contains(searchQuery, ignoreCase = true) }
                if (!isExisting) {
                    val suggestedUrl = if (searchQuery.contains(".")) searchQuery else "$searchQuery.com"
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(46.dp).background(SClrTealLight, CircleShape), contentAlignment = Alignment.Center) {
                                Text(suggestedUrl.first().uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = SClrTeal)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(suggestedUrl, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = SClrDark)
                            Button(
                                onClick = { tempSelectedSites.add(suggestedUrl); searchQuery = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = SClrTeal),
                                shape = RoundedCornerShape(20.dp), modifier = Modifier.height(36.dp)
                            ) { Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }
                        HorizontalDivider(color = SClrBg)
                    }
                }
            }

            // Sites List
            if (selectedTab == 1) {
                items(tempSelectedSites.size) { index ->
                    val site = tempSelectedSites[index]
                    if (searchQuery.isEmpty() || site.contains(searchQuery, ignoreCase = true)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp).clickable { 
                            if(tempSelectedSites.contains(site)) tempSelectedSites.remove(site) else tempSelectedSites.add(site)
                        }, verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = "https://www.google.com/s2/favicons?domain=${site}&sz=128",
                                contentDescription = null, modifier = Modifier.size(42.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(site, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SClrDark)
                                Text("Monitored Website", color = SClrGrayText, fontSize = 12.sp)
                            }
                            Checkbox(checked = true, onCheckedChange = { tempSelectedSites.remove(site) }, colors = CheckboxDefaults.colors(checkedColor = SClrTeal))
                        }
                        HorizontalDivider(color = SClrBg)
                    }
                }
            }
            
            // Apps List
            if (selectedTab == 0) {
                items(installedApps.size) { index ->
                    val app = installedApps[index]
                    if (searchQuery.isEmpty() || app.name.contains(searchQuery, ignoreCase = true)) {
                        val isSelected = tempSelectedApps.contains(app.packageName)
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp).clickable {
                            if(isSelected) tempSelectedApps.remove(app.packageName) else tempSelectedApps.add(app.packageName)
                        }, verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(46.dp).background(if(isSelected) SClrTealLight else SClrBg, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Android, contentDescription = null, tint = if(isSelected) SClrTeal else SClrGrayText)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SClrDark, maxLines = 1)
                                Text(app.packageName, color = SClrGrayText, fontSize = 11.sp, maxLines = 1)
                            }
                            Checkbox(checked = isSelected, onCheckedChange = { checked ->
                                if (checked) tempSelectedApps.add(app.packageName) else tempSelectedApps.remove(app.packageName)
                            }, colors = CheckboxDefaults.colors(checkedColor = SClrTeal))
                        }
                        HorizontalDivider(color = SClrBg)
                    }
                }
            }
        }

        // Sticky Save Button with Shadow
        Button(
            onClick = { onSave(tempSelectedApps, tempSelectedSites) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp).height(56.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = SClrTeal, spotColor = SClrTeal),
            colors = ButtonDefaults.buttonColors(containerColor = SClrTeal),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Save Settings", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SClrWhite)
        }
    }
}
