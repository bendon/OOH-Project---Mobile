package com.edgetech.bbscout.features.capture.presentation.review_data

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.R
import com.edgetech.bbscout.components.ui.ErrorShowDialog
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.utils.ifEmptySetNull
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
import com.edgetech.bbscout.features.capture.domain.model.BillboardTypeErrorException
import com.edgetech.bbscout.features.capture.domain.model.BrandDescriptionErrorException
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.model.LocationErrorException
import com.edgetech.bbscout.features.capture.domain.model.NoBillboardFoundException
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.capture.presentation.GetLocationComp
import com.edgetech.bbscout.features.capture.presentation.capture_detail.DataDetailHeading
import com.edgetech.bbscout.features.capture.presentation.capture_detail.DataDetailMultipleValues
import com.edgetech.bbscout.features.capture.presentation.capture_detail.DetailDataOneValue
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
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


    val captureRecordUiState by captureRecordUiModel.captureUiState.collectAsState()
    val currentData = getActiveBillboardData(captureRecordUiState)

    val analysingBillboard = captureRecordUiState.analysingLoading

    val currentBillBoadUiEvent by captureRecordUiModel.captureUiEvent.collectAsState()

    var selectedLocation by rememberSaveable {
        mutableStateOf<LatLng?>(null)
    }

    GetLocationComp(captureRecordUiModel, false)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation ?: LatLng(-0.0236, 37.9062), 5f)
    }

    selectedLocation = LatLng(
        captureRecordUiState.selectedLocation?.latitude ?: 0.0,
        captureRecordUiState.selectedLocation?.longitude ?: 0.0
    )
    cameraPositionState.position =
        CameraPosition.fromLatLngZoom(selectedLocation ?: LatLng(-0.0236, 37.9062), 5f)
    LaunchedEffect(recordId) {
//        captureRecordUiModel.captureEventSink(
//            CaptureRecordEventSink.OnAnalyseImage
//        )
    }

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Billboard", "Campain", "Location")




    if (currentBillBoadUiEvent is CaptureRecordUiEvent.CaptureRecordCreated) {
        LaunchedEffect(true) {
            appState?.navController?.popBackStack(AppDestinations.Dashboard, false)
        }
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.ResetUiEvent
        )

    }
    if (currentBillBoadUiEvent is CaptureRecordUiEvent.BillboardDataSet) {
        appState?.navController?.navigateUp()
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.ResetUiEvent
        )
    } else if (currentBillBoadUiEvent is CaptureRecordUiEvent.Error) {
        val request = (currentBillBoadUiEvent as CaptureRecordUiEvent.Error)
        if (request.exception is BrandDescriptionErrorException) {
            appState?.navController?.navigate(
                AppDestinations.EditCapture(
                    null,
                    RecordType.CAMPAIGN
                )
            )
        } else if (request.exception is BillboardTypeErrorException) {
            appState?.navController?.navigate(
                AppDestinations.EditCapture(
                    null,
                    RecordType.BILLBOARD_INFO
                )
            )
        } else {
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
                    if (request.exception is NoBillboardFoundException) {
                        appState?.navController?.navigateUp()
                    }
                },
                onPositive = { eventSink, ex ->
                    captureRecordUiModel.captureEventSink(
                        CaptureRecordEventSink.ResetUiEvent
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
    }

    if (analysingBillboard) {
        Dialog(onDismissRequest = {

        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {

                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }


        }
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
                if (currentData?.billboardImage != null) {
                    Card(
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                bitmap = currentData.billboardImage.asImageBitmap(),
                                contentDescription = "Cropped Billboard",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit

                            )
                            if (captureRecordUiState.analysingLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Center)
                                )
                            }
                        }
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    BillboardCaptureComp(
                        appState = appState,
                        currentData = currentData
                    )
                } else if (selectedTab == 1) {
                    CampaignCapture(
                        appState = appState,
                        currentData = currentData
                    )
                } else {
                    BillboardLocationComp(
                        selectedLocation = captureRecordUiState.selectedLocation,
                        selectedLoc = selectedLocation ?: LatLng(0.0, 0.0),
                        cameraPositionState = cameraPositionState
                    )
                }

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
//                OutlinedButton(
//                    onClick = {
//                        appState?.navController?.navigateUp()
//                    },
//                    shape = RoundedCornerShape(10.dp),
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("Retake photo")
//                }
                MainLoadingButton(
                    onTap = {
                        captureRecordUiModel.captureEventSink(
                            CaptureRecordEventSink.OnBillboardDataSet
                        )
                    },
                    modifier = Modifier.weight(1f),
                    pIsLoading = captureRecordUiState.isLoading
                ) {
                    Icon(
                        Icons.Default.CheckCircleOutline,
                        contentDescription = "Capture",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save", color = MaterialTheme.colorScheme.onPrimary)
                }


            }
        }

    }


}

@Composable
fun BillboardCaptureComp(
    appState: BBScoutAppState?,
    currentData: BillboardExtractedInfo?
) {

    ReviewDataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            DataDetailHeading(
                R.drawable.ic_billboard,
                "Billboard information",
                true,
                onEditClick = {
                    appState?.navController?.navigate(
                        AppDestinations.EditCapture(
                            null,
                            RecordType.BILLBOARD_INFO
                        )
                    )
                }
            )

            DetailDataOneValue(
                title = "Billboard owner",
                value = currentData?.billboardOwner.ifEmptySetNull() ?: "N/A",
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 4.dp)
                    .fillMaxWidth(),
            )

            DetailDataOneValue(
                title = "Billboard type",
                value = currentData?.billboardType?.ifEmptySetNull() ?: "N/A",

                )
            DataDetailMultipleValues(
                "Owner email",
                currentData?.ownerEmail
            )

            DataDetailMultipleValues(
                "Owner phone number",
                currentData?.phone?.map { it.toString() }
            )

            DetailDataOneValue(
                "Owner website",
                value = currentData?.ownerWebsite.ifEmptySetNull() ?: "N/A",
            )

        }
    }

    ReviewDataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            DataDetailHeading(
                R.drawable.billboard_svgrepo_com,
                "Billboard structure",
                true,
                onEditClick = {
                    appState?.navController?.navigate(
                        AppDestinations.EditCapture(
                            null,
                            RecordType.BILLBOARD_STRUCTURE
                        )
                    )
                }
            )

            DetailDataOneValue(
                title = "Billboard height",
                value = currentData?.billboardLength.ifEmptySetNull() ?: "N/A",

                )

            DetailDataOneValue(
                title = "Billboard width",
                value = currentData?.billboardWidth?.ifEmptySetNull() ?: "N/A",

                )

            DetailDataOneValue(
                title = "Billboard structure",
                value = currentData?.structure.ifEmptySetNull() ?: "N/A",

                )

            DetailDataOneValue(
                title = "Billboard material",
                value = currentData?.material.ifEmptySetNull() ?: "N/A",

                )

            DetailDataOneValue(
                title = "Billboard illumination",
                value = currentData?.illumination.ifEmptySetNull() ?: "N/A",
            )

            DetailDataOneValue(
                title = "Billboard visibility",
                value = currentData?.visibility.ifEmptySetNull() ?: "N/A",

                )

            DetailDataOneValue(
                title = "Billboard angle",
                value = currentData?.angle.ifEmptySetNull() ?: "N/A",

                )


        }
    }

}

@Composable
fun CampaignCapture(
    appState: BBScoutAppState?,
    currentData: BillboardExtractedInfo?
) {
    ReviewDataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            DataDetailHeading(
                R.drawable.megaphone_line_svgrepo_com,
                "Campaign information",
                true,
                onEditClick = {
                    appState?.navController?.navigate(
                        AppDestinations.EditCapture(
                            null,
                            RecordType.CAMPAIGN
                        )
                    )
                }
            )

            DetailDataOneValue(
                title = "Campaign brand",
                value = currentData?.brandName?.ifEmptySetNull() ?: "Unknown",
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 4.dp)
                    .fillMaxWidth(),
            )

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

            DetailDataOneValue(
                title = "Target age",
                value = currentData?.targetAge?.ifEmptySetNull()
                    ?: "Unknown",

                )

            DetailDataOneValue(
                title = "Target gender",
                value = currentData?.targetGender?.ifEmptySetNull()
                    ?: "Unknown",

                )

            DataDetailMultipleValues(
                "Products",
                currentData?.products,
            )

        }

    }
    ReviewDataCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            DataDetailHeading(
                R.drawable.communication_global_internet_svgrepo_com,
                "Communication channel",
                true,
                onEditClick = {
                    appState?.navController?.navigate(
                        AppDestinations.EditCapture(
                            null,
                            RecordType.CONTACT
                        )
                    )
                }
            )


            DataDetailMultipleValues(
                title = "Phone number",
                values = currentData?.phone?.map { it.toString() },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),

                )

            DataDetailMultipleValues(
                title = "Email",
                values = currentData?.email
            )

            DataDetailMultipleValues(
                title = "Website",
                values = currentData?.siteUrl
            )

            DataDetailMultipleValues(
                title = "Social media",
                values = currentData?.campainSocials
            )

        }
    }
}

@Composable
fun BillboardLocationComp(
    selectedLocation: UserLocationEntity?,
    selectedLoc: LatLng,
    cameraPositionState: CameraPositionState
) {
    ReviewDataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
            }

            Text(
                selectedLocation?.locationName ?: selectedLocation?.subAdminArea
                ?: "...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )

            Text(
                selectedLocation?.mainAdminArea ?: selectedLocation?.locationCity
                ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )

        }
    }

    ReviewDataCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            if (selectedLocation != null) {
                Marker(
                    state = MarkerState(position = selectedLoc),
                    title = "Your location",
                    snippet = "Location"
                )
            }
        }
    }
}