package com.edgetech.bbscout.features.capture.presentation.capture_flow.create_new_record

import android.annotation.SuppressLint
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.utils.logD
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.presentation.capture_flow.capture_data.CloseUpCapture
import com.edgetech.bbscout.features.capture.presentation.capture_flow.capture_data.LongSortCapture
import com.edgetech.bbscout.features.capture.presentation.capture_flow.get_inital_location.BillboardLocation
import com.edgetech.bbscout.features.capture.presentation.capture_flow.new_capture_review.AddBillboardContent
import com.edgetech.bbscout.features.capture.presentation.capture_flow.new_capture_review.ReviewCaptures
import com.edgetech.bbscout.features.capture.presentation.capture_flow.new_capture_review.SubmitNewCapture
import com.edgetech.bbscout.features.capture.presentation.capture_flow.select_billboard_type.SelectCaptureTypeMain
import com.edgetech.bbscout.features.capture_start.SelectBillboardSidesTypeMain
import com.edgetech.bbscout.features.navigation.AppDestinations
import kotlinx.serialization.Serializable
import androidx.navigation.NavDestination.Companion.hasRoute


@SuppressLint("RestrictedApi")
@Composable
fun NewCaptureNavHost(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel,
    modifier: Modifier = Modifier
) {
    val uiState by captureRecordUiModel.captureUiState.collectAsState()

    val newCaptureNavController = rememberNavController()
    val destination = uiState.newCaptureSelectedScreen
    LaunchedEffect(destination) {

        if (newCaptureNavController.currentBackStackEntry?.destination?.hierarchy?.any { it.hasRoute(destination::class) } != true)
        if (uiState.nextWasTapped == false){
            logD("Had entry")
            newCaptureNavController.popBackStack(destination, false)
        } else {
            newCaptureNavController.navigate(destination)
        }



    }
    NavHost(
        navController = newCaptureNavController,
        startDestination = NewCaptureDestinations.BillboardLocation,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        }
    ) {
        composable<NewCaptureDestinations.BillboardLocation> {
            BillboardLocation(captureRecordUiModel, appState)
        }

        composable<NewCaptureDestinations.SelectBillboardType> {
            SelectCaptureTypeMain(appState, captureRecordUiModel)
        }

        composable<NewCaptureDestinations.BillboardLongShot> {
            LongSortCapture(captureRecordUiModel, appState)
        }

        composable<NewCaptureDestinations.BillboardCloseUpShot> {
            CloseUpCapture(captureRecordUiModel, appState)
        }

        composable<NewCaptureDestinations.ConfirmAllDataCapture> {
            ReviewCaptures(captureRecordUiModel, appState)
        }

        composable<NewCaptureDestinations.SubmitPhysicalData> {
            SubmitNewCapture(captureRecordUiModel, appState)
        }

        composable<NewCaptureDestinations.SelectBillboardToAddContentTo> {
            AddBillboardContent(captureRecordUiModel, appState)
        }


    }
}

@SuppressLint("RestrictedApi")
fun NavController.hasDestination(des: NewCaptureDestinations): Boolean {
    logD("All entries are ${currentBackStack.value}")

    return this.currentBackStack.value .any { entry ->
        entry.destination.route == des.toString()
    }
}


sealed interface NewCaptureDestinations {


    @Serializable
    data object BillboardLocation : NewCaptureDestinations

    @Serializable
    data object SelectBillboardType : NewCaptureDestinations

    @Serializable
    data object BillboardLongShot : NewCaptureDestinations

    @Serializable
    data object BillboardCloseUpShot : NewCaptureDestinations

    @Serializable
    data object ConfirmAllDataCapture : NewCaptureDestinations

    @Serializable
    data object SubmitPhysicalData : NewCaptureDestinations

    @Serializable
    data object SelectBillboardToAddContentTo : NewCaptureDestinations


}