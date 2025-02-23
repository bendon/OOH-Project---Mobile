package com.edgetech.bbscout.features.capture.presentation.review_data

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.navOptions
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.location.GetLocationInfo
import com.edgetech.bbscout.components.utils.logD
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
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
fun EditRecordScreen(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel,
    recordId: Long?
) {
    EditRecordMain(
        appState = appState,
        captureRecordUiModel = captureRecordViewmodel.uiModel,
        recordId = recordId
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel,
    recordId: Long?
) {

    val context = LocalActivity.current as LocationAwareActivity

    val captureRecordUiState by captureRecordUiModel.captureUiState.collectAsState()
    val currentData = captureRecordUiState.billboardData

    val captureRecordUiEvent by captureRecordUiModel.captureUiEvent.collectAsState()

    var selectedLocation by rememberSaveable {
        mutableStateOf<LatLng?>(null)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation ?: LatLng(-0.0236, 37.9062), 5f)
    }

    var isGettingCurrentLocation by rememberSaveable {
        mutableStateOf(false)
    }

    val currentLocation by context.appLocation.observeAsState()

    var campaignBrand by rememberSaveable {
        mutableStateOf("")
    }

    var campaignDescription by rememberSaveable {
        mutableStateOf("")
    }


    var billboardType by rememberSaveable {
        mutableStateOf("")
    }

    var billboardOwner by rememberSaveable {
        mutableStateOf("")
    }

    var billboardWidth by rememberSaveable {
        mutableStateOf("")
    }

    var billboardLength by rememberSaveable {
        mutableStateOf("")
    }


    var locationInfo by remember {
        mutableStateOf<UserLocationEntity?>(null)
    }

    if (recordId == null) {
        //campaignDescription = currentData?.rawText ?: ""
        campaignBrand = currentData?.brandName ?: ""
        println("All brand data:  ${currentData?.brandName} ${currentData?.brandCampaign} ${currentData?.brandSlogan} ${currentData?.rawText}")
        if (currentData?.brandCampaign.isNullOrEmpty() && currentData?.brandSlogan.isNullOrEmpty()) {
            campaignDescription = currentData?.rawText ?: ""
        } else {
            campaignDescription = "${currentData?.brandCampaign} \n${currentData?.brandSlogan}"
        }
    }

    LaunchedEffect(true) {
        context.getLocation()
    }

    if (currentLocation != null || isGettingCurrentLocation) {
        isGettingCurrentLocation = false
        LaunchedEffect(currentLocation) {
            selectedLocation = currentLocation?.toLatLng()
            selectedLocation?.let {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
                GetLocationInfo.getLocationInfo(context, it) { loc ->
                    locationInfo = loc
                }

            }
        }
    }


    if (captureRecordUiEvent is CaptureRecordUiEvent.CaptureRecordCreated) {

        appState?.navController?.navigate(AppDestinations.BillboardAdded, navOptions = navOptions {
            popUpTo(AppDestinations.Dashboard)
        })
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
                Text(
                    text = "Full image",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                if (currentData?.fullImage != null) {

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Image(
                            bitmap = currentData.fullImage.asImageBitmap(),
                            contentDescription = "Cropped Billboard",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
                Text(
                    text = "Billboard image",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                if (currentData?.billboardImage != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Image(
                            bitmap = currentData?.billboardImage!!.asImageBitmap(),
                            contentDescription = "Cropped Billboard",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }


                Text(
                    text = "Campaign information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                OutlinedTextField(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    value = campaignBrand,
                    onValueChange = { campaignBrand = it },
                    label = { Text("Campaign brand") }
                )

                OutlinedTextField(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    value = campaignDescription,
                    onValueChange = { campaignDescription = it },
                    label = { Text("Campaign description") })

                Text(
                    text = "Billboard information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                OutlinedTextField(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    value = billboardType,
                    onValueChange = { billboardType = it },
                    label = { Text("Billboard type") })

                OutlinedTextField(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    value = billboardOwner,
                    onValueChange = { billboardOwner = it },
                    label = { Text("Billboard owner") })

                Row(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 2.dp),
                        value = billboardWidth,
                        onValueChange = { billboardWidth = it },
                        label = { Text("Width (m)") })
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 2.dp),
                        value = billboardLength,
                        onValueChange = { billboardLength = it },
                        label = { Text("Height (m)") })
                }

                //check qr code

                if (!currentData?.qrCode.isNullOrEmpty()) {
                    Text(
                        text = "QR code",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    currentData?.qrCode?.forEach {
                        Text(text = it, modifier = Modifier.padding(vertical = 6.dp))
                    }
                }


                //check other info
                if (!currentData?.entityInfos.isNullOrEmpty()) {
                    Text(text = "Other information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    currentData?.entityInfos?.forEach {
                        Text(
                            text = "${it.type}: ${it.text}",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

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
                    locationInfo?.locationCity ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    locationInfo?.subAdminArea ?: locationInfo?.mainAdminArea ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
                Button(
                    onClick = {
                        captureRecordUiModel.captureEventSink(
                            CaptureRecordEventSink.OnSaveCapture(
                                billboardData = BillboardDataEntity(
                                    type = billboardType,
                                    owner = billboardOwner,
                                    width = billboardWidth.toDoubleOrNull() ?: 0.0,
                                    height = billboardLength.toDoubleOrNull() ?: 0.0,
                                ),
                                location = locationInfo,
                                brandName = campaignBrand,
                                campaignDescription = campaignDescription,
                                qrCode = currentData?.qrCode ?: emptyList(),
                                entityInfos = currentData?.entityInfos ?: emptyList(),

                                )
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircleOutline, contentDescription = "Capture")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit")
                }


            }

        }

    }


}