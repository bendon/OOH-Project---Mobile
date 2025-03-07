package com.edgetech.bbscout.features.capture.presentation.capture_flow.create_new_record

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.navigation.AppDestinations
import kotlinx.serialization.Serializable


@Composable
fun NewCaptureNavHost(
    appState: BBScoutAppState,
    captureRecordUiModel: CaptureRecordUiModel
){

    val newCaptureNavController = rememberNavController()

    NavHost(
        navController = newCaptureNavController,
        startDestination = NewCaptureDestinations.BillboardLocation
    ){
        composable<NewCaptureDestinations.BillboardLocation> {
           TODO("add this screen")
        }

        composable<NewCaptureDestinations.SelectBillboardType> {
            TODO("add this screen")
        }

        composable<NewCaptureDestinations.BillboardLongShot> {
            TODO("add this screen")
        }

        composable<NewCaptureDestinations.BillboardCloseUpShot> {
            TODO("add this screen")
        }

        composable<NewCaptureDestinations.ConfirmAllDataCapture> {
            TODO("add this screen")
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