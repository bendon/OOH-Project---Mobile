package com.edgetech.bbscout.features.capture.presentation.capture_flow.capture_data

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.presentation.GetLocation
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CameraComp
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CapturedImage
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CreateCaptureGroupHeading
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.DistanceComposable
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InPageAlert


@Composable
fun LongSortCapture(
    captureRecordUiModel: CaptureRecordUiModel,
    appState: BBScoutAppState?
) {

    GetLocation {
        captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnSetLocation(it))
    }
    val uiState by captureRecordUiModel.captureUiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        CreateCaptureGroupHeading(
            Icons.Outlined.CameraAlt,
            "Capture Long short",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        DistanceComposable(
            title = "GPS Location",
            distance = uiState.billboardData?.distanceFromBillboard,
            minDistance = 20.0,
            maxDistance = 100.0,
            isValid = uiState.billboardData?.isDistanceValid ?: false,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (uiState.billboardData?.status != true) {
            CameraComp(
                onCaptureTaken = { uri ->
                    captureRecordUiModel.captureEventSink(
                        CaptureRecordEventSink.OnCaptureEvent(
                            fileUri = uri.path ?: ""
                        )
                    )
                },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .height(220.dp)
            )
        } else {
            if (uiState.billboardData?.billboardImage != null) {
                CapturedImage(
                    image = uiState.billboardData?.billboardImage!!,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .height(220.dp)
                )
            }
        }

        InPageAlert(
            isSuccess = false,
            isError = false,
            title = "Take a log short photo",
            message = "Take a photo of the billboard from a distance of 20-100 meters",
            modifier = Modifier.padding(bottom = 16.dp)
        )


        if (uiState.billboardData?.status == true)
            InPageAlert(
                isSuccess = true,
                isError = false,
                title = "Long shot captured",
                message = "Long shot captured successfully",
                onNext = {
                    captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnUiNext)
                },
            )

    }

}