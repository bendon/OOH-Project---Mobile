package com.edgetech.bbscout.features.capture.presentation.review_data

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.ErrorShowDialog
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.utils.ifEmptySetNull
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.UnAuthenticatedException
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.auth.domain.model.AuthUiEvent
import com.edgetech.bbscout.features.auth.domain.model.EmptyCredentialsException
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.example.core.core.utils.components.LocationAwareActivity
import com.example.core.core.utils.components.toLatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun ReviewRecordScreen(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel,
    recordId: Long?
) {
    ReviewRecordMain(
        appState = appState,
        captureRecordUiModel = captureRecordViewmodel.uiModel,
        recordId = recordId
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewRecordMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel,
    recordId: Long?
) {

    val context = LocalActivity.current as LocationAwareActivity
    val captureRecordUiState by captureRecordUiModel.captureUiState.collectAsState()
    val currentData = captureRecordUiState.billboardData

    val currentBillBoadUiEvent by captureRecordUiModel.captureUiEvent.collectAsState()

    var selectedLocation by rememberSaveable {
        mutableStateOf<LatLng?>(null)
    }

    var isGettingCurrentLocation by rememberSaveable {
        mutableStateOf(false)
    }

    val currentLocation by context.appLocation.observeAsState()


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation ?: LatLng(-0.0236, 37.9062), 5f)
    }



    LaunchedEffect(true) {
        context.getLocation()
    }

    if (currentLocation != null || isGettingCurrentLocation) {
        isGettingCurrentLocation = false
        LaunchedEffect(currentLocation) {
            selectedLocation = currentLocation?.toLatLng()
            if (selectedLocation != null) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLocation!!, 5f)
                captureRecordUiModel.captureEventSink(
                    CaptureRecordEventSink.OnSetLocation(selectedLocation!!)
                )
            }
        }
    }

    if (currentBillBoadUiEvent is CaptureRecordUiEvent.CaptureRecordCreated){
        LaunchedEffect(true) {
            appState?.navController?.popBackStack(AppDestinations.Dashboard, false)
        }
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.ResetState
        )

    } else if(currentBillBoadUiEvent is CaptureRecordUiEvent.Error){
        val request = (currentBillBoadUiEvent as CaptureRecordUiEvent.Error)
        ErrorShowDialog(
            showErrorMessage = true,
            customError = mapOf(

            ),
            error = request.exception,
            event = request.eventSink,
            onDismiss = {
                captureRecordUiModel.captureEventSink(
                    CaptureRecordEventSink.ResetState
                )
            },
            onPositive = { eventSink, ex ->
                captureRecordUiModel.captureEventSink(
                    CaptureRecordEventSink.ResetState
                )
//                if (ex !is EmptyCredentialsException && eventSink != null){
//                    authUiModel.authEventSink(eventSink as AuthEventSink)
//                } else {
//                    authUiModel.authEventSink(
//                        AuthEventSink.ResetState
//                    )
//                }
            }

        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                    .copy(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(text = "Verify") },
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (currentData?.fullImage != null) {
                    Card(
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Image(
                            bitmap = currentData.fullImage.asImageBitmap(),
                            contentDescription = "Cropped Billboard",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
                Card(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Campaign information",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Icon(Icons.Outlined.Edit, "", tint = Color.Gray, modifier = Modifier.clickable {
                                appState?.navController?.navigate(AppDestinations.EditCapture(null, RecordType.CAMPAIGN))
                            })
                        }

                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 4.dp)
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
                                text = currentData?.brandName?.ifEmptySetNull()
                                    ?: "Unknown",
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
                            text = currentData?.brandCampaign.ifEmptySetNull() ?: "Unknown",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }

                }
                Card(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Billboard information",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Icon(Icons.Outlined.Edit, "", tint = Color.Gray, modifier = Modifier.clickable {
                                appState?.navController?.navigate(AppDestinations.EditCapture(null, RecordType.BILLBOARD_INFO))
                            })
                        }

                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 4.dp)
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
                                text = currentData?.billboardOwner.ifEmptySetNull() ?: "N/A",
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
                                text = currentData?.billboardType?.ifEmptySetNull() ?: "N/A",
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
                                text = currentData?.billboardLength.ifEmptySetNull() ?: "N/A",
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
                                text = currentData?.billboardWidth?.ifEmptySetNull() ?: "N/A",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

//                Card(
//                    shape = MaterialTheme.shapes.small,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 16.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        Row(
//                            verticalAlignment = CenterVertically,
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Text(
//                                text = "Other content",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                            )
//                            Icon(Icons.Outlined.Edit, "", tint = Color.Gray)
//                        }
//                        currentData?.entityInfos?.forEach { data ->
//                            Row(
//                                modifier = Modifier
//                                    .padding(top = 6.dp)
//                                    .fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                Text(
//                                    text = data.type.ifEmptySetNull() ?: "N/A",
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Normal,
//                                    modifier = Modifier.padding(end = 16.dp)
//                                )
//                                Text(
//                                    text = data.text.ifEmptySetNull() ?: "N/A",
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Bold,
//                                )
//                            }
//                        }
//
//                    }
//                }

                Card(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Location",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Text(
                            captureRecordUiState.selectedLocation?.locationName ?: captureRecordUiState.selectedLocation?.subAdminArea
                            ?: "...",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )

                        Text(
                            captureRecordUiState.selectedLocation?.mainAdminArea ?: captureRecordUiState.selectedLocation?.locationCity
                            ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                    }
                }

                Card(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                ){
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


            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        appState?.navController?.navigateUp()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retake photo")
                }
                MainLoadingButton(
                    onTap = {
                        captureRecordUiModel.captureEventSink(
                            CaptureRecordEventSink.OnSaveCapture()
                        )
                    },
                    modifier = Modifier.weight(1f),
                   pIsLoading = captureRecordUiState.isLoading
                ) {
                    Icon(Icons.Default.CheckCircleOutline, contentDescription = "Capture")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit")
                }


            }
        }

    }


}