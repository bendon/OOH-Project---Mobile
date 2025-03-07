package com.edgetech.bbscout.features.capture.presentation.capture_flow.capture_data

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
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.presentation.GetLocation
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CameraComp
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CapturedImage
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CreateCaptureGroupHeading
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.DimensionDetectedComp
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.DistanceComposable
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InPageAlert
import com.edgetech.bbscout.features.capture.presentation.review_data.getActiveBillboardData


@Composable
fun CloseUpCapture(
    captureRecordUiModel: CaptureRecordUiModel,
    appState: BBScoutAppState?
) {

    GetLocation {
        captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnSetLocation(it))
    }
    val uiState by captureRecordUiModel.captureUiState.collectAsState()
    val currentData = getActiveBillboardData(uiState)

    CreateCaptureGroupHeading(
        Icons.Outlined.CameraAlt,
        "Capture Close-Up ${currentData?.billboardSideInfo?.displayName ?: ""}",
        modifier = Modifier.padding(vertical = 8.dp)
    )

    DistanceComposable(
        title = "GPS Location (${currentData?.billboardSideInfo?.displayName ?: ""})",
        distance = currentData?.distanceFromBillboard,
        minDistance = 5.0,
        maxDistance = 20.0,
        isValid = currentData?.isDistanceValid ?: false,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (currentData?.status != true) {
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
        if (currentData.billboardImage != null) {
            CapturedImage(
                image = currentData.billboardImage,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .height(220.dp)
            )
        }
    }

    DimensionDetectedComp(
        unit = currentData?.unitOfMeasurement ?: "meters",
        width = currentData?.billboardWidth?.toDoubleOrNull(),
        height = currentData?.billboardLength?.toDoubleOrNull(),
        onConfirm = { width, height ->
            captureRecordUiModel.captureEventSink(
                CaptureRecordEventSink.OnEditCaptureEvent(
                    currentData?.copy(
                        billboardWidth = width?.toString() ?: "",
                        billboardLength = height?.toString() ?: ""
                    ) ?: BillboardExtractedInfo(
                        billboardWidth = width?.toString() ?: "",
                        billboardLength = height?.toString() ?: ""
                    )
                )
            )
        },
        modifier = Modifier.padding(bottom = 16.dp)
    )

    InPageAlert(
        isSuccess = false,
        isError = false,
        title = "Take a close-up photo",
        message = "Take a photo of the billboard from a distance of 5-20 meters",
        modifier = Modifier.padding(bottom = 16.dp)
    )

    if (currentData?.status == true)
        InPageAlert(
            isSuccess = true,
            isError = false,
            title = "Close-Up captured",
            message = "Close-Up ${currentData.billboardSideInfo?.displayName ?: ""} captured successfully",
            onNext = {
                captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnUiNext)
            },
        )




}