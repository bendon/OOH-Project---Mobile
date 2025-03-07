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
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.ui.NonLoadingSecButton
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel


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
                .padding(horizontal = 16.dp)
        ) {
            if(uiState.newCaptureNumberOfSteps != null){
                Text(
                    text = "Step ${uiState.newCaptureCurrentStep } of ${uiState.newCaptureNumberOfSteps}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                )
                BBScoutStepper(
                    numberOfSteps = uiState.newCaptureNumberOfSteps!!,
                    currentStep = uiState.newCaptureCurrentStep,
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                )
            }
            NewCaptureNavHost(
                appState,
                captureRecordUiModel,
                modifier = Modifier.weight(1f)
            )
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.surface,
            )
            Row(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
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