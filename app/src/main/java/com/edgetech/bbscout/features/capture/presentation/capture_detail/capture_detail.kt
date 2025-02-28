package com.edgetech.bbscout.features.capture.presentation.capture_detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.R
import com.edgetech.bbscout.components.utils.ifEmptySetNull
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


@Composable
fun CaptureDetailScreen(
    captureId: String,
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel = hiltViewModel()
) {
    CaptureDetailMain(
        captureId = captureId,
        appState = appState,
        captureRecordUiModel = captureRecordViewmodel.uiModel
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun CaptureDetailMain(
    captureId: String,
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel
) {

    val captureUiState by captureRecordUiModel.captureUiState.collectAsState()

    var selectedCapture: EntryRecord? = null
    if (captureUiState.selectedRecord?.entryEntity?.remoteId == captureId) {
        selectedCapture = captureUiState.selectedRecord
    }

    var selectedLocation by rememberSaveable {
        mutableStateOf<LatLng?>(null)
    }


    selectedLocation = LatLng(
        selectedCapture?.location?.latitude ?: 0.0,
        selectedCapture?.location?.longitude ?: 0.0
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation ?: LatLng(-0.0236, 37.9062), 5f)
    }

    LaunchedEffect(selectedLocation) {
        cameraPositionState.position =
            CameraPosition.fromLatLngZoom(selectedLocation ?: LatLng(-0.0236, 37.9062), 10f)
    }



    LaunchedEffect(captureId) {
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.OnGetCapture(captureId)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                    .copy(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(text = "Billboard") },
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
        Column {
            if (captureUiState.isLoading){
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {

                if (captureUiState.selectedRecord?.entryEntity?.remoteFileUrl != null) {
                    Text(
                        text = "Full image",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        GlideImage(
                            model = "https://scout.edgetech.co.ke/api/v1/auth/file/${captureUiState.selectedRecord?.entryEntity?.remoteFileUrl}",
                            contentDescription = "",
                            modifier = Modifier.fillMaxSize(),
                        )
//                        Image(
//                            captureUiState.selectedRecordMainImage!!.asImageBitmap(),
//                            contentDescription = "Cropped Billboard",
//                            modifier = Modifier.fillMaxSize(),
//                            contentScale = ContentScale.FillBounds
//                        )
                    }
                }

                if (captureUiState.selectedRecordBillboardImage != null) {
                    Text(
                        text = "Billboard image",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Image(
                            captureUiState.selectedRecordBillboardImage!!.asImageBitmap(),
                            contentDescription = "Cropped Billboard",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }

                CaptureBillboardInfo(
                    selectedCapture = selectedCapture
                )

                Text(
                    text = "Billboard location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(width = 1.dp, color = Color.LightGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        if (selectedLocation != null) {
                            Marker(
                                state = MarkerState(position = selectedLocation!!),
                                title = "Your location",
                                snippet = "Location"
                            )
                        }
                    }
                }
                Text(
                    selectedCapture?.location?.locationName
                        ?: selectedCapture?.location?.subAdminArea
                        ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    selectedCapture?.location?.mainAdminArea
                        ?: selectedCapture?.location?.locationCity
                        ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}


@Composable
fun CaptureBillboardInfo(selectedCapture: EntryRecord?){
    Text(
        text = "Campaign information",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Campaign brand",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = selectedCapture?.entryEntity?.brand.ifEmptySetNull() ?: "Unknown",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Text(
        text = "Campaign",
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(top = 0.dp)
    )
    Text(
        text = selectedCapture?.entryEntity?.augmentedText.ifEmptySetNull()
            ?: selectedCapture?.entryEntity?.rawText.ifEmptySetNull() ?: "Unknown",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(end = 16.dp)
    )
    Text(
        text = "Billboard information",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Billboard owner",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = selectedCapture?.billboardData?.owner.ifEmptySetNull() ?: "N/A",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Billboard type",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = selectedCapture?.billboardData?.type.ifEmptySetNull() ?: "N/A",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Billboard height",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = selectedCapture?.billboardData?.height?.toString() ?: "N/A",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Billboard width",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = selectedCapture?.billboardData?.width.toString() ?: "N/A",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
    if (!selectedCapture?.otherData.isNullOrEmpty()) {
        Text(
            text = "Others",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        selectedCapture?.otherData?.forEach { data ->
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = data.type ?: data.key ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = data.value ?: "N/A",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

    }
}



@Preview
@Composable
fun CaptureDetailMainPreview() {
    CaptureDetailMain("", null, CaptureRecordUiModel())
}
