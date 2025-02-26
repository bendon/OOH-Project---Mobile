package com.edgetech.bbscout.features.capture.presentation.capture_listing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun CapturesListingScreen(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel = hiltViewModel(),
){
    CapturesListingMain(
        appState = appState,
        captureRecordUiModel = captureRecordViewmodel.uiModel
    )
}



@Composable
fun CapturesListingMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel,
){

    val capturesUiState by captureRecordUiModel.captureUiState.collectAsState()

    val allEntries = capturesUiState.allCaptures

    LaunchedEffect(true) {
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.OnGetAllCaptures
        )
    }

    Scaffold  {
        LazyColumn(modifier = Modifier.padding(it).padding(top = 32.dp).padding(horizontal = 16.dp)) {
            items(allEntries.size) {
                val item = allEntries[it]
                BillboardListingItem(
                    item,
                    onTap = {
                        appState?.navController?.navigate(AppDestinations.CaptureDetail(item.entryEntity.remoteId ?: ""))
                    }
                )
            }
        }
    }



}

