package com.edgetech.bbscout.features.navigation

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.utils.getPreference
import com.edgetech.bbscout.features.capture_start.CaptureCheckPermission
import com.edgetech.bbscout.features.dashboard.BBScoutDashboard
import com.edgetech.bbscout.features.dashboard.HomeDashboard
import com.edgetech.bbscout.features.onboarding_screen.OnboardingScreen
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Composable
fun DirackAppNavigation(
    appState: BBScoutAppState?,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val navController = appState?.navController

    val hasShownOnboarding = getPreference(context, "ONBOARDING_SHOWN", Boolean::class.java )

    NavHost(
        modifier = modifier,
        navController = navController!!,
        startDestination = if (hasShownOnboarding == true) AppDestinations.Dashboard else AppDestinations.OnboardingPage
    ){
        composable<AppDestinations.Dashboard>{
            BBScoutDashboard(appState = appState)
        }

        composable<AppDestinations.OnboardingPage>{
            OnboardingScreen(appState = appState)
        }

    }

}

@Composable
fun BBScoutDashboardNavigation(
    appState: BBScoutAppState?,
    modifier: Modifier = Modifier
){

    appState?.dashboardNavController = rememberNavController()
    val navController = appState?.dashboardNavController

    NavHost(
        modifier = modifier,
        navController = navController!!,
        startDestination = DashboardScreenOption.HOME.name
    ){
        composable(
            route = DashboardScreenOption.HOME.name
        ) {
            HomeDashboard(appState)
        }

        composable(
            route = DashboardScreenOption.CAPTURE.name
        ) {
            CaptureCheckPermission(appState)
        }

        composable(
            route = DashboardScreenOption.HISTORY.name
        ) {

        }
        composable(
            route = DashboardScreenOption.SETTINGS.name
        ) {

        }

    }


}

enum class DashboardScreenOption(val route: String, val iconUnselected: ImageVector, val iconSelected: ImageVector, val label: String) {
    HOME("home", Icons.Outlined.Home, Icons.Filled.Home, "Home"),
    CAPTURE("capture", Icons.Outlined.PhotoCamera, Icons.Filled.PhotoCamera,"Capture"),
    HISTORY("history", Icons.Outlined.History, Icons.Filled.History, "History"),
    SETTINGS("settings", Icons.Outlined.Settings, Icons.Filled.Settings, "Settings")
}

sealed interface AppDestinations {


    @Serializable
    data object OnboardingPage : AppDestinations

    @Serializable
    data object Dashboard : AppDestinations

}