package com.edgetech.bbscout.features.capture_start

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.CompSelectableState
import com.edgetech.bbscout.components.ui.SelectorComposable
import com.edgetech.bbscout.features.capture.domain.model.BillboardSides
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
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

    if (uiEvent is CaptureRecordUiEvent.StartBillBoardSurvey) {
        val side = (uiEvent as CaptureRecordUiEvent.StartBillBoardSurvey).billboard
        //if (side !=  BillboardSides.MAIN ){
        appState?.navController?.navigate(AppDestinations.CameraCapture(side))
        // }
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.ResetState
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
                            Text(
                                if (uiState.createBillboardSideCount == 1) "Billboard close up" else "Side one",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Take a close up of the billboard image of the billboard",
                                modifier = Modifier.padding(vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    status =  CompSelectableState.SELECTED
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
                                Text("Side two", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Take a close up of the billboard image of the billboard",
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        status =  CompSelectableState.SELECTED
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
                                Text("Side three", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Take a close up of the billboard image of the billboard",
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        status =  CompSelectableState.SELECTED
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
                                Text("Side four", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Take a close up of the billboard image of the billboard",
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                        },
                        status =  CompSelectableState.SELECTED
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
                        captureRecordUiModel.captureEventSink(
                            CaptureRecordEventSink.StartBillBoardSurvey(BillboardSides.SIDE_FOUR)
                        )
                    },
                    content = {
                        Column {
                            Text("Wide shot", style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    status = CompSelectableState.SELECTED
                )
            }

        }
    }

}
