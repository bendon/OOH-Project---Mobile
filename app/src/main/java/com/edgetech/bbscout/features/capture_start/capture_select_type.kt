package com.edgetech.bbscout.features.capture_start

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.CompSelectableState
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.ui.SelectorComposable
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun SelectBillboardSidesTypeScreen(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel
) {
    SelectBillboardSidesTypeMain(
        appState = appState,
        captureRecordUiModel = captureRecordViewmodel.uiModel
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBillboardSidesTypeMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel
) {


    var selectedType by rememberSaveable {
        mutableStateOf(1)
    }

    val uiEvent by captureRecordUiModel.captureUiEvent.collectAsState()
     if (uiEvent is CaptureRecordUiEvent.BillboardNumberOfSideSet){
         appState?.navController?.navigate(AppDestinations.CaptureDashboard)
         captureRecordUiModel.captureEventSink(
             CaptureRecordEventSink.ResetState
         )
     }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                    .copy(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(text = "Number of sides") },
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
            modifier = Modifier.padding(it).padding(horizontal = 16.dp)
        ) {
            Text(
                text = "How many sides does this billboard have",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                SelectorComposable(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    onTap = {
                        selectedType = 1
                    },
                    content = "1",
                    status = if (selectedType == 1) CompSelectableState.SELECTED else CompSelectableState.NOT_SELECTED
                )

                SelectorComposable(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    onTap = {
                        selectedType = 2
                    },
                    content = "2",
                    status = if (selectedType == 2) CompSelectableState.SELECTED else CompSelectableState.NOT_SELECTED
                )
                SelectorComposable(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    onTap = {
                        selectedType = 3
                    },
                    content = "3",
                    status = if (selectedType == 3) CompSelectableState.SELECTED else CompSelectableState.NOT_SELECTED
                )
                SelectorComposable(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    onTap = {
                        selectedType = 4
                    },
                    content = "4",
                    status = if (selectedType == 4) CompSelectableState.SELECTED else CompSelectableState.NOT_SELECTED
                )
            }
            MainLoadingButton(
                loadOnTap = false,
                disableOnTap = false,
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                onTap = {
                    captureRecordUiModel.captureEventSink(
                        CaptureRecordEventSink.SetBillboardNumberOfSide(selectedType)
                    )
                }
            ) {
                Text("Next", color = MaterialTheme.colorScheme.onPrimary)
            }

        }
    }

}