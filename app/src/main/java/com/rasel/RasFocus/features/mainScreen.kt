package com.rasel.RasFocus.features

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.navigation.NavController

// ==========================================
// C++ Premium Palette (From Image)
// ==========================================
private val ColGradientStart = Color(0xFF4F46E5) // গাঢ় নীল
private val ColGradientEnd = Color(0xFF0CA8B0)   // টিল (Teal)
private val ColBgContent = Color(0xFFF4F7FA)     // হালকা গ্রে ব্যাকগ্রাউন্ড
private val ColTextDark = Color(0xFF1E293B)      // ডার্ক টেক্সট
private val ColGreenText = Color(0xFF10B981)     // অ্যানালিটিক্স এর সবুজ টেক্সট

@Composable
fun MainScreen(navController: NavController, onOpenDrawer: () -> Unit) {
    val context = LocalContext.current
    // বটম নেভিগেশনের স্টেট
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColBgContent)
    ) {
        // ==========================================
        // স্ক্রোলযোগ্য বডি কন্টেন্ট
        // ==========================================
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ১. গ্রেডিয়েন্ট হেডার (কার্ভ করা নিচের অংশ)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(Brush.horizontalGradient(listOf(ColGradientStart, ColGradientEnd)))
                    .statusBarsPadding() // স্ট্যাটাস বারের জায়গা ছাড়ার জন্য
                    .padding(top = 16.dp, bottom = 40.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // হ্যামবার্গার মেনু আইকন
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(26.dp))
                    }

                    // নোটিফিকেশন আইকন
                    IconButton(
                        onClick = { Toast.makeText(context, "No new notifications", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Alerts", tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
            }

            // ২. গ্রিটিংস ও ব্যাটারি পারমিশন কার্ড
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Welcome", fontSize = 16.sp, color = Color.Gray)
                Text("Good Morning", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = ColTextDark)
                
                Spacer(modifier = Modifier.height(24.dp))

                // ব্যাটারি অপটিমাইজেশন কার্ড (Light Pinkish)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECEE)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "Grant the following permissions for Stay\nFocused to work properly!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColTextDark,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = "Close", 
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { Toast.makeText(context, "Closed", Toast.LENGTH_SHORT).show() }
                                    .padding(4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.BatteryAlert, contentDescription = null, tint = ColTextDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Battery Optimisation", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f), color = ColTextDark)
                            Button(
                                onClick = { Toast.makeText(context, "Opening Settings...", Toast.LENGTH_SHORT).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Disable", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ৩. অ্যানালিটিক্স সেকশন
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF64748B))
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // অ্যানালিটিক্স গ্রিড কার্ড
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Screen Time Card
                    AnalyticsCard(
                        title = "Screen Time",
                        mainValue = "05 sec",
                        subValue = "-99 percent",
                        icon = Icons.Outlined.Alarm,
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate("statistics") 
                    }
                    
                    // App Launches Card
                    AnalyticsCard(
                        title = "App Launches",
                        mainValue = "1",
                        subValue = "-363 launches",
                        icon = Icons.Outlined.RocketLaunch,
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate("statistics") 
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ৪. Take a Break Card (Light Purple)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate("deep_study") },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE9D5FF)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.LocalCafe, contentDescription = null, tint = Color(0xFF4B0082), modifier = Modifier.size(30.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Take a Break", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4B0082))
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp)) // স্ক্রোল করার জন্য বাফার স্পেস
            }
        }

        // ==========================================
        // ৫. প্রিমিয়াম বটম নেভিগেশন বার
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding() // নিচের সিস্টেম নেভিগেশন বারের সাথে যেন মিশে না যায়
                .padding(vertical = 12.dp, horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem("Dashboard", Icons.Outlined.Dashboard, selectedBottomTab == 0) { 
                    selectedBottomTab = 0 
                }
                BottomNavItem("Modes", Icons.Outlined.Bolt, selectedBottomTab == 1) { 
                    selectedBottomTab = 1
                    navController.navigate("blocks")
                }
                BottomNavItem("Analytics", Icons.Outlined.BarChart, selectedBottomTab == 2) { 
                    selectedBottomTab = 2
                    navController.navigate("statistics")
                }
                BottomNavItem("Account", Icons.Outlined.Person, selectedBottomTab == 3) { 
                    selectedBottomTab = 3 
                    navController.navigate("settings")
                }
            }
        }
    }
}

// ==========================================
// রি-ডিজাইন করা অ্যানালিটিক্স কার্ড
// ==========================================
@Composable
fun AnalyticsCard(title: String, mainValue: String, subValue: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = ColTextDark, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(title, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(mainValue, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = ColTextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subValue, fontSize = 12.sp, color = ColGreenText, fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("View", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = ColTextDark)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = ColTextDark)
            }
        }
    }
}

// ==========================================
// বটম নেভিগেশন আইটেম ডিজাইন (Pill Shape)
// ==========================================
@Composable
fun BottomNavItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFFE0E7FF) else Color.Transparent // অ্যাক্টিভ থাকলে হালকা নীল
    val contentColor = if (isSelected) Color(0xFF4F46E5) else Color.Gray   // অ্যাক্টিভ টেক্সট গাঢ় নীল

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .height(36.dp)
                .width(64.dp)
                .background(bgColor, RoundedCornerShape(18.dp)), // পিল (Pill) শেপ
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = contentColor)
    }
}
