package com.edgetech.bbscout

import com.edgetech.bbscout.screens.MapScreen
import com.edgetech.bbscout.screens.CaptureScreen
import com.edgetech.bbscout.screens.HistoryScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.edgetech.bbscout.ui.theme.BBScoutTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.edgetech.bbscout.data.BillboardRepository
import com.edgetech.bbscout.screens.BillboardCapture
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BBScoutTheme {
                AppContent()
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Map : Screen("map", Icons.Filled.LocationOn, "Map")
    object Capture : Screen("capture", Icons.Filled.PhotoCamera, "Capture")
    object History : Screen("history", Icons.Filled.History, "History")
}

@Composable
private fun AppContent() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Map, Screen.Capture, Screen.History)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Map.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Capture.route) { CaptureScreen() }
            composable(Screen.History.route) {
                val repository = remember { BillboardRepository(context = LocalContext.current) }
                val scope = rememberCoroutineScope()
                var captures by remember { mutableStateOf<List<BillboardCapture>>(emptyList()) }

                // Load captures when screen is shown
                LaunchedEffect(Unit) {
                    captures = repository.getAllCaptures()
                }

                HistoryScreen(
                    captures = captures,
                    onCaptureClick = { capture ->
                        // Handle capture click if needed
                    },
                    onDeleteCapture = { capture ->
                        scope.launch {
                            repository.deleteCapture(capture)
                            captures = repository.getAllCaptures()
                        }
                    }
                )
            }
        }
    }
}