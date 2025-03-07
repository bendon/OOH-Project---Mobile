package com.edgetech.bbscout.features.capture.presentation.capture_flow.new_capture_review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.ButtonContent
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.ui.NonLoadingSecButton
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CreateCaptureGroupHeading
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InfoComposableContainer
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InfoComposableType
import com.edgetech.bbscout.ui.theme.mainBlue


@Composable
fun SubmitNewCapture(
    captureRecordUiModel: CaptureRecordUiModel,
    appState: BBScoutAppState?,
){
    val uiState by captureRecordUiModel.captureUiState.collectAsState()

    Column {
        CreateCaptureGroupHeading(
            Icons.Outlined.InsertDriveFile,
            "Content identification",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        InfoComposableContainer(
            InfoComposableType.BORDER_GRAY,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "You have now completed the physical structure. Proceed to identify the contend of the billboard.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                )

                InfoComposableContainer(
                    InfoComposableType.BLUE_CONTAINER,
                    modifier = Modifier.padding(8.dp)
                ){
                    Icon(Icons.Outlined.InsertDriveFile, contentDescription = null, tint = mainBlue, modifier = Modifier.padding(8.dp).size(48.dp))
                }

                MainLoadingButton(
                    pIsLoading = uiState.isLoading,
                    onTap = {

                    },
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp).fillMaxWidth()
                ) {
                    ButtonContent(
                        "Continue to content identification",
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }

                NonLoadingSecButton(
                    isLoading = uiState.isLoading,
                    onTap = { captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnSaveCapture()) },
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp).fillMaxWidth()
                ) {
                    ButtonContent(
                        "Save capture",
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }


            }

        }

        InfoComposableContainer(
            InfoComposableType.BORDER_GRAY,
            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
        ){
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Captured information",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
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

    }


}