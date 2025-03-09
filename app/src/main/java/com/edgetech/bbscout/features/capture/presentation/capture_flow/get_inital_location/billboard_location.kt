package com.edgetech.bbscout.features.capture.presentation.capture_flow.get_inital_location

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.ButtonContent
import com.edgetech.bbscout.components.ui.NonLoadingSecButton
import com.edgetech.bbscout.components.ui.StatusDialog
import com.edgetech.bbscout.components.ui.WarningIcon
import com.edgetech.bbscout.components.utils.isDebug
import com.edgetech.bbscout.components.utils.logD
import com.edgetech.bbscout.data.utils.DataConstants
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.presentation.GetLocation
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.CreateCaptureGroupHeading
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.GpsLocationComp
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InPageAlert
import com.edgetech.bbscout.ui.theme.mainGreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


@Composable
fun BillboardLocation(
    captureRecordUiModel: CaptureRecordUiModel,
    appState: BBScoutAppState?
) {

    GetLocation {

        captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnSetLocation(it))
    }

    val uiState by captureRecordUiModel.captureUiState.collectAsState()

    var askToStartNewEntry by rememberSaveable {
        mutableStateOf(false)
    }

    if (askToStartNewEntry){
        StatusDialog(
            isVisible = true,
            onDismissRequest = {
                askToStartNewEntry = false
            },
            onConfirmation = {
               captureRecordUiModel.captureEventSink(
                   CaptureRecordEventSink.ResetCreatingCapture
               )
            },
            title = "Start new entry",
            message = "Are you sure you to start a new entry, all data saved so far will be removed",
            posText = "Yes",
            negText = "Cancel",
            icon = {
                WarningIcon()
            }
        )
    }
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
            message = "Move as close as possible to the billboard. Once the location information has been fetched tap on 'Lock location'",
            modifier = Modifier.padding(bottom = 16.dp),
            nextText = "Lock location",
            onNext = if (uiState.selectedLocation?.latitude != null && uiState.selectedLocation?.isLocationLocked != true) {
                {
                    captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnLockBillboardLocation)
                }
            } else
                null,
        )


        if (uiState.newCaptureNextIsEnabled)
            InPageAlert(
                isSuccess = true,
                isError = false,
                title = "Location captured",
                message = "Billboard location captured successfully",
                onNext = {
                    captureRecordUiModel.captureEventSink(CaptureRecordEventSink.OnUiNext)
                },
            )
        Spacer(
            modifier = Modifier.weight(1f)
        )

        Button(
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, color = Color.Red),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = Color.Red
            ),
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            onClick = {
                askToStartNewEntry = true
            }
        ) {

            Text(
                "Start a new entry"
            )
        }




    }


}

@Preview
@Composable
fun BillboardLocationPreview() {
    BillboardLocation(
        captureRecordUiModel = CaptureRecordUiModel(),
        appState = null
    )
}