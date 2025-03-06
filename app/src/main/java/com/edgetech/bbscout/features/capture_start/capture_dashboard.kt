package com.edgetech.bbscout.features.capture_start

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.R
import com.edgetech.bbscout.components.ui.CompSelectableState
import com.edgetech.bbscout.components.ui.ErrorShowDialog
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.ui.SelectorComposable
import com.edgetech.bbscout.features.capture.domain.model.BillboardSides
import com.edgetech.bbscout.features.capture.domain.model.BillboardTypeErrorException
import com.edgetech.bbscout.features.capture.domain.model.BrandDescriptionErrorException
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiState
import com.edgetech.bbscout.features.capture.domain.model.LocationErrorException
import com.edgetech.bbscout.features.capture.domain.model.NoBillboardFoundException
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.capture.presentation.review_data.RecordType
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun BillboardDataUploadDashboardScreen(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel
) {
    BillboardDataUploadDashboardMain(
        appState = appState,
        captureRecordUiModel = captureRecordViewmodel.uiModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillboardDataUploadDashboardMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel
) {

    val uiState by captureRecordUiModel.captureUiState.collectAsState()
    val uiEvent by captureRecordUiModel.captureUiEvent.collectAsState()


    if (uiEvent is CaptureRecordUiEvent.CaptureRecordCreated) {
        LaunchedEffect(true) {
            appState?.navController?.popBackStack(AppDestinations.Dashboard, false)
        }
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.ResetUiEvent
        )
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.ResetCreatingCapture
        )

    }
    else if (uiEvent is CaptureRecordUiEvent.StartBillBoardSurvey) {
            val side = (uiEvent as CaptureRecordUiEvent.StartBillBoardSurvey).billboard
            //if (side !=  BillboardSides.MAIN ){
            appState?.navController?.navigate(AppDestinations.CameraCapture(side))
            // }
            captureRecordUiModel.captureEventSink(
                CaptureRecordEventSink.ResetUiEvent
            )
        }
    else if (uiEvent is CaptureRecordUiEvent.ContinueBillBoardSurvey) {
            val side = (uiEvent as CaptureRecordUiEvent.ContinueBillBoardSurvey).billboard
            appState?.navController?.navigate(AppDestinations.ReviewBillboardData)
            captureRecordUiModel.captureEventSink(
                CaptureRecordEventSink.ResetUiEvent
            )
        }
    else if (uiEvent is CaptureRecordUiEvent.Error) {
        val request = (uiEvent as CaptureRecordUiEvent.Error)

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
                    if (eventSink != null)
                    captureRecordUiModel.captureEventSink(
                        eventSink as CaptureRecordEventSink
                    )
                }

            )

    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                    .copy(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(text = "Survey") },
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
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Select next step",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {

                Spacer(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )
                SelectorComposable(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    onTap = {
                        captureRecordUiModel.captureEventSink(
                            CaptureRecordEventSink.StartBillBoardSurvey(BillboardSides.SIDE_ONE)
                        )
                    },
                    content = {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (uiState.createBillboardSideCount == 1) "Billboard close up" else "Side one",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (uiState.sideOneExtractedInfo?.status == true)
                                    Image(
                                        painter = painterResource(R.drawable.check_circle_svgrepo_com),
                                        contentDescription = "Check",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(4.dp),
                                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                                    )
                            }
                            Text(
                                "Take a close up of the billboard image of the billboard",
                                modifier = Modifier.padding(vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    status = CompSelectableState.SELECTED
                )

                if (uiState.createBillboardSideCount > 1) {
                    SelectorComposable(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                        onTap = {
                            captureRecordUiModel.captureEventSink(
                                CaptureRecordEventSink.StartBillBoardSurvey(BillboardSides.SIDE_TWO)
                            )
                        },
                        content = {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Side two", style = MaterialTheme.typography.titleMedium,   modifier = Modifier.weight(1f)
                                    )
                                    if (uiState.sideTwoExtractedInfo?.status == true)
                                        Image(
                                            painter = painterResource(R.drawable.check_circle_svgrepo_com),
                                            contentDescription = "Check",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .padding(4.dp),
                                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                                        )
                                }
                                Text(
                                    "Take a close up of the billboard image of the billboard",
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        status = CompSelectableState.SELECTED
                    )
                }

                if (uiState.createBillboardSideCount > 2) {
                    SelectorComposable(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                        onTap = {
                            captureRecordUiModel.captureEventSink(
                                CaptureRecordEventSink.StartBillBoardSurvey(BillboardSides.SIDE_THREE)
                            )
                        },
                        content = {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Side three", style = MaterialTheme.typography.titleMedium,   modifier = Modifier.weight(1f)
                                    )
                                    if (uiState.sideThreeExtractedInfo?.status == true)
                                        Image(
                                            painter = painterResource(R.drawable.check_circle_svgrepo_com),
                                            contentDescription = "Check",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .padding(4.dp),
                                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                                        )
                                }
                                Text(
                                    "Take a close up of the billboard image of the billboard",
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        status = CompSelectableState.SELECTED
                    )
                }

                if (uiState.createBillboardSideCount > 3) {
                    SelectorComposable(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                        onTap = {
                            captureRecordUiModel.captureEventSink(
                                CaptureRecordEventSink.StartBillBoardSurvey(BillboardSides.SIDE_FOUR)
                            )
                        },
                        content = {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Side four", style = MaterialTheme.typography.titleMedium,   modifier = Modifier.weight(1f)
                                    )
                                    if (uiState.sideFourExtractedInfo?.status == true)
                                        Image(
                                            painter = painterResource(R.drawable.check_circle_svgrepo_com),
                                            contentDescription = "Check",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .padding(4.dp),
                                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                                        )
                                }
                                Text(
                                    "Take a close up of the billboard image of the billboard",
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                        },
                        status = CompSelectableState.SELECTED
                    )
                }

                Text(
                    text = "Take a wide perspective of the billboard",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )
                SelectorComposable(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    onTap = {
                        if (shouldEnableWidePic(uiState))
                        captureRecordUiModel.captureEventSink(
                            CaptureRecordEventSink.StartBillBoardSurvey(BillboardSides.MAIN)
                        )
                    },
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Wide shot",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.billboardData?.status == true)
                                Image(
                                    painter = painterResource(R.drawable.check_circle_svgrepo_com),
                                    contentDescription = "Check",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp),
                                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                                )
                        }
                    },
                    status = if (shouldEnableWidePic(uiState)) CompSelectableState.SELECTED else CompSelectableState.NOT_SELECTED
                )
            }

            MainLoadingButton(
                onTap = {
                    captureRecordUiModel.captureEventSink(
                        CaptureRecordEventSink.OnSaveCapture()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                pIsLoading = uiState.isLoading,
                isEnabled = shouldEnableButton(uiState)
            ) {
                Icon(
                    Icons.Default.CheckCircleOutline,
                    contentDescription = "Capture",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

}

fun shouldEnableWidePic(uiState: CaptureRecordUiState): Boolean {
    when(uiState.createBillboardSideCount){
        1 -> {
            return uiState.sideOneExtractedInfo?.status == true
        }
        2 -> {
            return uiState.sideOneExtractedInfo?.status == true && uiState.sideTwoExtractedInfo?.status == true
        }
        3 -> {
            return uiState.sideOneExtractedInfo?.status == true && uiState.sideTwoExtractedInfo?.status == true && uiState.sideThreeExtractedInfo?.status == true
        }
        4 -> {
            return uiState.sideOneExtractedInfo?.status == true && uiState.sideTwoExtractedInfo?.status == true && uiState.sideThreeExtractedInfo?.status == true && uiState.sideFourExtractedInfo?.status == true
        }
        else -> {
            return false
        }
    }
}

fun shouldEnableButton(uiState: CaptureRecordUiState): Boolean {
    return shouldEnableWidePic(uiState) && uiState.billboardData?.status == true
}
