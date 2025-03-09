package com.edgetech.bbscout.features.capture.presentation.capture_flow.create_new_record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.ButtonContent
import com.edgetech.bbscout.components.ui.ErrorShowDialog
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.ui.NonLoadingSecButton
import com.edgetech.bbscout.components.ui.StatusDialog
import com.edgetech.bbscout.components.ui.SuccessIcon
import com.edgetech.bbscout.components.utils.logD
import com.edgetech.bbscout.features.capture.domain.model.BillboardTypeErrorException
import com.edgetech.bbscout.features.capture.domain.model.BrandDescriptionErrorException
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.model.LocationErrorException
import com.edgetech.bbscout.features.capture.domain.model.NoBillboardFoundException
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.capture.presentation.review_data.RecordType
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun NewCaptureScreen(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel
){
    NewCaptureParentMain(
        appState,
        captureRecordViewmodel.uiModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCaptureParentMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel
){

    val uiState by captureRecordUiModel.captureUiState.collectAsState()

    val uiEvent by captureRecordUiModel.captureUiEvent.collectAsState()

    if (uiEvent is CaptureRecordUiEvent.Error) {

        val request = (uiEvent as CaptureRecordUiEvent.Error)

        if (request.exception is BillboardTypeErrorException){
            appState?.navController?.navigate(
                AppDestinations.EditCapture(
                    null,
                    RecordType.BILLBOARD_INFO
                )
            )
        }
        else {
            ErrorShowDialog(
                showErrorMessage = true,
                customError = mapOf(
                    BillboardTypeErrorException to "Billboard type is required",
                    LocationErrorException to "Location is required",
                    BrandDescriptionErrorException to "Brand description is required",
                    NoBillboardFoundException to "No billboard detected"
                ),
                error = request.exception,
                event = request.eventSink,
                onDismiss = {
                    captureRecordUiModel.captureEventSink(
                        CaptureRecordEventSink.ResetUiEvent
                    )
                },
                onPositive = { eventSink, ex ->
                    captureRecordUiModel.captureEventSink(
                        CaptureRecordEventSink.ResetUiEvent
                    )
                }

            )
        }
    } else if (uiEvent is CaptureRecordUiEvent.CaptureRecordCreated){
        StatusDialog(
            icon = {
                SuccessIcon()
            },
            isVisible = true,
            title = "Success",
            message = "Capture record created successfully",
            onDismissRequest = {
                captureRecordUiModel.captureEventSink(
                    CaptureRecordEventSink.ResetUiEvent
                )
                captureRecordUiModel.captureEventSink(
                    CaptureRecordEventSink.ResetCreatingCapture
                )
                appState?.navController?.navigateUp()
            },
            posText = "Ok",
            onConfirmation = {

            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                    .copy(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(text = "New capture") },
                navigationIcon = {
                    IconButton(onClick = {
                        appState?.navController?.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate up",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },

                )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
        ) {
            if(uiState.newCaptureNumberOfSteps != null){
                Text(
                    text = "Step ${uiState.newCaptureCurrentStep } of ${uiState.newCaptureNumberOfSteps}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = 4.dp,)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                )
                BBScoutStepper(
                    numberOfSteps = uiState.newCaptureNumberOfSteps!!,
                    currentStep = uiState.newCaptureCurrentStep,
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                )
            }
            NewCaptureNavHost(
                appState,
                captureRecordUiModel,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.surface,
            )
            Row(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                if (uiState.newCaptureCurrentStep != 1)
                NonLoadingSecButton(
                    isEnabled = uiState.newCaptureBackEnabled,
                    onTap = {
                        captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnUiBack)
                    },
                ) {
                    ButtonContent(
                        title = "Back",
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (uiState.newCaptureCurrentStep != uiState.newCaptureNumberOfSteps)
                MainLoadingButton (
                    isEnabled = uiState.newCaptureNextIsEnabled,
                    onTap = {
                        captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnUiNext)
                    },
                ) {
                    ButtonContent(
                        title = uiState.newCaptureNextText,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

    }

}