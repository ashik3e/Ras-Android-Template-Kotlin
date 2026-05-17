package com.rasel.RasFocus.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*

// ==========================================
// App Color Constants (shared across files)
// ==========================================
val ColTeal = Color(0xFF0CA8B0)
val ColGradientStart = Color(0xFF4F46E5)
val ColGradientEnd = Color(0xFF0CA8B0)
val ColBgContent = Color(0xFFF4F7FA)

// ==========================================
// Root Composable — NavGraph + Drawer
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RasFocusApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: "dashboard"

    val onNavigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo("dashboard") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(ColGradientStart, ColGradientEnd))
                    )
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    "RasFocus",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                SidebarItem("Dashboard", Icons.Default.Dashboard, "dashboard", currentRoute, onNavigate)
                SidebarItem("App Blocks", Icons.Default.Shield, "blocks", currentRoute, onNavigate)
                SidebarItem("Adult Block", Icons.Default.Lock, "adult_block", currentRoute, onNavigate)
                SidebarItem("Deep Study", Icons.Default.MenuBook, "deep_study", currentRoute, onNavigate)
                SidebarItem("Statistics", Icons.Default.Assessment, "statistics", currentRoute, onNavigate)
                SidebarItem("Settings", Icons.Default.Settings, "settings", currentRoute, onNavigate)
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {
                MainScreen(navController = navController, onOpenDrawer = {
                    scope.let {
                        // open drawer on click
                    }
                })
            }
            composable("blocks") { Blocks() }
            composable("adult_block") { Adult_block() }
            composable("deep_study") { Deep_study() }
            composable("statistics") { Statistics() }
            composable("settings") { Settings() }
        }
    }
}

// ==========================================
// Sidebar Navigation Item
// ==========================================
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
            .padding(horizontal = 16.dp, vertical = 4.dp)
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
