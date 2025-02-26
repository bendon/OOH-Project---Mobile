package com.edgetech.bbscout

import com.edgetech.bbscout.screens.MapScreen
import com.edgetech.bbscout.screens.HistoryScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.setValue
import com.diracks.app.app.app_state.rememberBBScoutAppState
import com.edgetech.bbscout.features.capture.presentation.camera_capture.CaptureBillboardScreen
import com.edgetech.bbscout.features.navigation.DirackAppNavigation
import com.example.core.core.utils.components.LocationAwareActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : LocationAwareActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            val appState = rememberBBScoutAppState(
               // windowSizeClass = calculateWindowSizeClass(this),
                //networkMonitor = networkMonitor,
                // dashboardNavController = rememberNavController(),
//                timeZoneMonitor = timeZoneMonitor,
//                appType = if (checkIfIsOnPatientApp()) MyrekodAppType.MYREKOD_APP else MyrekodAppType.CHV_APP,
//                userAccountAppState = UserAccountAppState().copy(
//                    appId = getApplicationPackagedName()
//                ),
//                activity = this
            )
            BBScoutTheme {
                //BillboardDetectorScreen()
               // CaptureBillboardScreen(null)
                // CameraMLApp()
                DirackAppNavigation(appState = appState)
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

    val context = LocalContext.current
    var captures by remember { mutableStateOf<List<BillboardCapture>>(emptyList()) }

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
           // composable(Screen.Capture.route) { CaptureScreen() }
            composable(Screen.History.route) {
                val repository = remember { BillboardRepository(context = context) }
                val scope = rememberCoroutineScope()


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