package com.edgetech.bbscout.features.capture.presentation.capture_flow.new_capture_review

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.ButtonContent
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.features.capture.domain.model.BillboardSides
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CreateCaptureGroupHeading
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InPageAlert
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.NewCaptureImageReview
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun AddBillboardContent(
    captureRecordUiModel: CaptureRecordUiModel,
    appState: BBScoutAppState?
){
    val uiState by captureRecordUiModel.captureUiState.collectAsState()

    BoxWithConstraints {
        LazyVerticalGrid(
            columns = if (maxWidth < 600.dp) GridCells.Fixed(2) else GridCells.Fixed(4),
        ) {
            item(
                key = "review_captures_heading",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Column {
                    CreateCaptureGroupHeading(
                        Icons.Outlined.TextSnippet,
                        "Identify the content",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            items(uiState.createBillboardSideCount){
                when(it){
                    0 -> {
                        NewCaptureImageReview(
                            uiState.sideOneExtractedInfo?.billboardImage,
                            "Side 1",
                            modifier = Modifier.padding(2.dp),
                            onTap = {
                                captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnStartSideContentVerification(BillboardSides.SIDE_ONE))
                                appState?.navController?.navigate(AppDestinations.ReviewBillboardData)
                            }
                        )
                    }
                    1 -> {
                        NewCaptureImageReview(
                            uiState.sideTwoExtractedInfo?.billboardImage,
                            "Side 2",
                            modifier = Modifier.padding(2.dp),
                            onTap = {
                                captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnStartSideContentVerification(BillboardSides.SIDE_TWO))
                                appState?.navController?.navigate(AppDestinations.ReviewBillboardData)
                            }
                        )
                    }
                    2 -> {
                        NewCaptureImageReview(
                            uiState.sideThreeExtractedInfo?.billboardImage,
                            "Side 3",
                            modifier = Modifier.padding(2.dp),
                            onTap = {
                                captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnStartSideContentVerification(BillboardSides.SIDE_THREE))
                                appState?.navController?.navigate(AppDestinations.ReviewBillboardData)
                            }
                        )
                    }
                    3 -> {
                        NewCaptureImageReview(
                            uiState.sideFourExtractedInfo?.billboardImage,
                            "Side 4",
                            modifier = Modifier.padding(2.dp),
                            onTap = {
                                captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnStartSideContentVerification(BillboardSides.SIDE_FOUR))
                                appState?.navController?.navigate(AppDestinations.ReviewBillboardData)
                            }
                        )
                    }
                }
            }
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Column {

                    InPageAlert(
                        isSuccess = false,
                        isError = false,
                        title = "Review billboard and submit.",
                        message = "To review and edit the billboard content, tap the image of the side. After making any necessary updates, tap 'Submit' to save your changes.",
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    MainLoadingButton(
                        pIsLoading = uiState.isLoading,
                        onTap = { captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnSaveCapture()) },
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
                    ) {
                        ButtonContent(
                            "Submit",
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                }
            }

        }
    }

}