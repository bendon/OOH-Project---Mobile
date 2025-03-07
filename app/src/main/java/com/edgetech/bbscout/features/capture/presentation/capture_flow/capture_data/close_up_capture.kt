package com.edgetech.bbscout.features.capture.presentation.capture_flow.capture_data

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.model.closeUpDistance
import com.edgetech.bbscout.features.capture.presentation.GetLocation
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CameraComp
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CapturedImage
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CreateCaptureGroupHeading
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.DimensionDetectedComp
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.DistanceComposable
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.GpsLocationComp
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CreateCaptureGroupHeading(
            Icons.Outlined.CameraAlt,
            "Capture Close-Up ${currentData?.billboardSideInfo?.displayName ?: ""}",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        GpsLocationComp(
            lat = currentData?.billboardLocation?.latitude,
            lng = currentData?.billboardLocation?.longitude,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DistanceComposable(
            title = "GPS Location (${currentData?.billboardSideInfo?.displayName ?: ""})",
            distance = currentData?.distanceFromBillboard,
            minDistance = closeUpDistance.start,
            maxDistance = closeUpDistance.endInclusive,
            isValid = currentData?.isDistanceValid ?: false,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (currentData?.closedUpUri == null) {
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
        }
        else if (currentData?.billboardImage != null) {
            Box {
                CapturedImage(
                    image = currentData.billboardImage,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .height(220.dp)
                )
                if (uiState.analysingLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)

                    )
                }
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
                onRetry = {
                    captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnRecapture)
                }
            )

    }
}