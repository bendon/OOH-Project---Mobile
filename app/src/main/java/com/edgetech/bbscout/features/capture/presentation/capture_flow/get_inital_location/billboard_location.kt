package com.edgetech.bbscout.features.capture.presentation.capture_flow.get_inital_location

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.presentation.GetLocation
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CreateCaptureGroupHeading
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.GpsLocationComp
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InPageAlert


@Composable
fun BillboardLocation(
    captureRecordUiModel: CaptureRecordUiModel,
    appState: BBScoutAppState?
){

    GetLocation{
        captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnSetLocation(it))
    }

    val uiState by captureRecordUiModel.captureUiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CreateCaptureGroupHeading(
            Icons.Outlined.LocationOn,
            "Billboard Location",
            modifier = Modifier.padding(bottom = 8.dp)
        )


        GpsLocationComp(
            lat = uiState.selectedLocation?.latitude,
            lng = uiState.selectedLocation?.longitude,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        InPageAlert(
            isSuccess = false,
            isError = false,
            title = "Get billboard location",
            message = "Move as close as possible to the billboard",
            modifier = Modifier.padding(bottom = 16.dp)
        )


        if (uiState.selectedLocation?.latitude != null && uiState.selectedLocation?.longitude != null)
        InPageAlert(
            isSuccess = true,
            isError = false,
            title = "Location captured",
            message = "Billboard location captured successfully",
            onNext = {
                captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnUiNext)
            },
        )



    }


}

@Preview
@Composable
fun BillboardLocationPreview(){
    BillboardLocation(
        captureRecordUiModel = CaptureRecordUiModel(),
        appState = null
    )
}