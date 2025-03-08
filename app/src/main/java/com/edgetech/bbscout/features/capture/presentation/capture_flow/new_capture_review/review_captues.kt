package com.edgetech.bbscout.features.capture.presentation.capture_flow.new_capture_review

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CapturedImage
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CreateCaptureGroupHeading
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InPageAlert
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InfoComposableContainer
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InfoComposableType
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.NewCaptureImageReview


@Composable
fun ReviewCaptures(
    captureRecordUiModel: CaptureRecordUiModel,
    appState: BBScoutAppState?
){

    val uiState by captureRecordUiModel.captureUiState.collectAsState()

    BoxWithConstraints  {
        LazyVerticalGrid(
                columns = if (maxWidth < 600.dp) GridCells.Fixed(2) else GridCells.Fixed(4) ,
        ) {
            item(
                key = "review_captures_heading",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Column {
                    CreateCaptureGroupHeading(
                        Icons.Outlined.Visibility,
                        "Review Captures",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    InfoComposableContainer(InfoComposableType.BLUE_CONTAINER, modifier = Modifier.padding(bottom = 8.dp)) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CreateCaptureGroupHeading(
                                Icons.Outlined.InsertDriveFile,
                                "Captures summary",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Type:")
                                }
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                    append(uiState.newBillboardType ?: "")
                                }
                            }, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                            Text(buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Sides:")
                                }
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                    append(uiState.createBillboardSideCount.toString() ?: "")
                                }
                            }, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))

                            Text(buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Long short GPS:")
                                }
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                    append("${uiState.billboardData?.billboardLocation?.latitude}, ${uiState.billboardData?.billboardLocation?.longitude}")
                                }
                            }, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))



                        }
                    }
                    Text(
                        text = "Long shot",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }


            }
            item(
                key = "long_shot",
                span = { if (maxWidth < 600.dp) GridItemSpan(maxLineSpan) else GridItemSpan(2) }
            ) {
                NewCaptureImageReview(
                    uiState.billboardData?.billboardImage,
                    "Long shot",
                )
            }
            item(
                key = "close_up_title",
                span = { GridItemSpan(maxLineSpan) }
            ){
                Text(
                    text = "Close-Up",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            items(uiState.createBillboardSideCount){
                when(it){
                    0 -> {
                        NewCaptureImageReview(
                            uiState.sideOneExtractedInfo?.billboardImage,
                            "Side 1",
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                    1 -> {
                        NewCaptureImageReview(
                            uiState.sideTwoExtractedInfo?.billboardImage,
                            "Side 2",
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                    2 -> {
                        NewCaptureImageReview(
                            uiState.sideThreeExtractedInfo?.billboardImage,
                            "Side 3",
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                    3 -> {
                        NewCaptureImageReview(
                            uiState.sideFourExtractedInfo?.billboardImage,
                            "Side 4",
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
            item(
                key = "review_captures_alert",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                InPageAlert(
                    isSuccess = true,
                    isError = false,
                    title = "Physical capture completed",
                    message = "All required captures have been taken. Proceed to upload.",
                    onNext = {
                        captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnUiNext)
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}