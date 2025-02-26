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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.utils.getPreference
import com.edgetech.bbscout.features.auth.domain.viewmodel.AuthViewmodel
import com.edgetech.bbscout.features.auth.presentation.LoadingScreen
import com.edgetech.bbscout.features.auth.presentation.LoginScreen
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.capture.presentation.camera_capture.CaptureBillboardScreen
import com.edgetech.bbscout.features.capture.presentation.capture_detail.CaptureDetailScreen
import com.edgetech.bbscout.features.capture.presentation.capture_listing.CapturesListingScreen
import com.edgetech.bbscout.features.capture.presentation.review_data.EditRecordScreen
import com.edgetech.bbscout.features.capture.presentation.review_data.OnDataAdded
import com.edgetech.bbscout.features.capture.presentation.review_data.RecordType
import com.edgetech.bbscout.features.capture.presentation.review_data.ReviewRecordScreen
import com.edgetech.bbscout.features.capture_start.CaptureCheckPermission
import com.edgetech.bbscout.features.dashboard.BBScoutDashboard
import com.edgetech.bbscout.features.dashboard.HomeDashboard
import com.edgetech.bbscout.features.onboarding_screen.OnboardingScreen
import com.edgetech.bbscout.features.settings.presentation.SettingsScreen
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

    val captureRecordViewmodel = viewModel<CaptureRecordViewmodel>()
    val authViewModel = viewModel<AuthViewmodel>()



    NavHost(
        modifier = modifier,
        navController = navController!!,
        startDestination = if (hasShownOnboarding == true) AppDestinations.Loading else AppDestinations.OnboardingPage
    ){
        composable<AppDestinations.Dashboard>{
            BBScoutDashboard(appState = appState)
        }

        composable<AppDestinations.OnboardingPage>{
            OnboardingScreen(appState = appState)
        }

        composable<AppDestinations.CameraCapture> {
            CaptureBillboardScreen(
                appState,
                captureRecordViewmodel
            )
        }

        composable<AppDestinations.EditCapture> {
            val args = it.toRoute<AppDestinations.EditCapture>()
            EditRecordScreen(
                appState,
                captureRecordViewmodel,
                args.id,
                args.recordType
            )
        }

        composable<AppDestinations.CaptureDetail> {
            val args = it.toRoute<AppDestinations.CaptureDetail>()
            CaptureDetailScreen(
                args.id,
                appState
            )

        }

        composable<AppDestinations.BillboardAdded> {
            OnDataAdded(appState)
        }

        composable<AppDestinations.ReviewBillboardData> {
            ReviewRecordScreen(appState, captureRecordViewmodel, null)
        }

        composable<AppDestinations.Login> {
            LoginScreen(authViewModel, navController)
        }

        composable<AppDestinations.Loading> {
            LoadingScreen(authViewModel, navController)
        }

    }

}

@Composable
fun BBScoutDashboardNavigation(
    appState: BBScoutAppState?,
    modifier: Modifier = Modifier,
    onPageTap: (DashboardScreenOption) -> Unit = {},
){

    appState?.dashboardNavController = rememberNavController()
    val navController = appState?.dashboardNavController
    val authViewModel = hiltViewModel<AuthViewmodel>()
    NavHost(
        modifier = modifier,
        navController = navController!!,
        startDestination = DashboardScreenOption.HOME.name
    ){
        composable(
            route = DashboardScreenOption.HOME.name
        ) {
            HomeDashboard(appState, onPageTap = onPageTap)
        }

        composable(
            route = DashboardScreenOption.CAPTURE.name
        ) {
            CaptureCheckPermission(appState)
        }

        composable(
            route = DashboardScreenOption.HISTORY.name
        ) {
            CapturesListingScreen(appState)
        }
        composable(
            route = DashboardScreenOption.SETTINGS.name
        ) {
            SettingsScreen(authViewModel = authViewModel, appState)
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

    @Serializable
    data object CameraCapture : AppDestinations

    @Serializable
    data class CaptureDetail(val id: String) : AppDestinations

    @Serializable
    data class EditCapture(val id: Long?, val recordType: RecordType) : AppDestinations

    @Serializable
    data object ReviewBillboardData : AppDestinations

    @Serializable
    data object BillboardAdded :  AppDestinations

    @Serializable
    data object Login : AppDestinations

    @Serializable
    data object Loading : AppDestinations

}