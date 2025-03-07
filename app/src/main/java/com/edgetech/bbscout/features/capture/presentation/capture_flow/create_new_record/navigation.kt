package com.edgetech.bbscout.features.capture.presentation.capture_flow.create_new_record

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.presentation.capture_flow.capture_data.CloseUpCapture
import com.edgetech.bbscout.features.capture.presentation.capture_flow.capture_data.LongSortCapture
import com.edgetech.bbscout.features.capture.presentation.capture_flow.get_inital_location.BillboardLocation
import com.edgetech.bbscout.features.capture.presentation.capture_flow.new_capture_review.ReviewCaptures
import com.edgetech.bbscout.features.capture.presentation.capture_flow.new_capture_review.SubmitNewCapture
import com.edgetech.bbscout.features.capture.presentation.capture_flow.select_billboard_type.SelectCaptureTypeMain
import com.edgetech.bbscout.features.capture_start.SelectBillboardSidesTypeMain
import com.edgetech.bbscout.features.navigation.AppDestinations
import kotlinx.serialization.Serializable


@Composable
fun NewCaptureNavHost(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel,
    modifier: Modifier = Modifier
){
    val uiState by captureRecordUiModel.captureUiState.collectAsState()

    val newCaptureNavController = rememberNavController()
    val destination = uiState.newCaptureSelectedScreen
    NavHost(
        navController = newCaptureNavController,
        startDestination = destination,
        modifier = modifier
    ){
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





}