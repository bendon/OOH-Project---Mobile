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
import com.edgetech.bbscout.components.utils.ifEmptySetNull
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
    recordId: Long?,
    recordType: RecordType
) {
    EditRecordMain(
        appState = appState,
        captureRecordUiModel = captureRecordViewmodel.uiModel,
        recordId = recordId,
        recordType
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel,
    recordId: Long?,
    recordType: RecordType
) {

    val context = LocalActivity.current as LocationAwareActivity

    val captureRecordUiState by captureRecordUiModel.captureUiState.collectAsState()
    val currentData = captureRecordUiState.billboardData

    val captureRecordUiEvent by captureRecordUiModel.captureUiEvent.collectAsState()



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


    LaunchedEffect(
        recordId
    ) {
        if (recordId == null) {
            campaignBrand = currentData?.brandName.ifEmptySetNull() ?: ""
            campaignDescription = currentData?.brandCampaign.ifEmptySetNull() ?: ""
            billboardType = currentData?.billboardType.ifEmptySetNull() ?: ""
            billboardOwner = currentData?.billboardOwner.ifEmptySetNull() ?: ""
            billboardWidth = currentData?.billboardWidth.ifEmptySetNull() ?: ""
            billboardLength = currentData?.billboardLength.ifEmptySetNull() ?: ""
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

                if (recordType == RecordType.CAMPAIGN) {

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
                }
                if (recordType == RecordType.BILLBOARD_INFO) {
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
                }
                //check qr c





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
                Button(
                    onClick = {
                        captureRecordUiModel.captureEventSink(
                            CaptureRecordEventSink.OnCaptureEvent(
                                billboardData = currentData?.copy(
                                    brandName = campaignBrand.ifEmptySetNull() ?: currentData.brandName,
                                    brandCampaign = campaignDescription.ifEmptySetNull() ?: currentData.brandCampaign,
                                    billboardType = billboardType.ifEmptySetNull() ?: currentData.billboardType,
                                    billboardOwner = billboardOwner.ifEmptySetNull() ?: currentData.billboardOwner,
                                    billboardWidth = billboardWidth.ifEmptySetNull() ?: currentData.billboardWidth,
                                    billboardLength = billboardLength.ifEmptySetNull() ?: currentData.billboardLength
                                ) ?: BillboardExtractedInfo(
                                    brandName = campaignBrand.ifEmptySetNull() ?: currentData?.brandName,
                                    brandCampaign = campaignDescription.ifEmptySetNull() ?: currentData?.brandCampaign,
                                    billboardType = billboardType.ifEmptySetNull() ?: currentData?.billboardType,
                                    billboardOwner = billboardOwner.ifEmptySetNull() ?: currentData?.billboardOwner,
                                    billboardWidth = billboardWidth.ifEmptySetNull() ?: currentData?.billboardWidth,
                                    billboardLength = billboardLength.ifEmptySetNull() ?: currentData?.billboardLength
                                )
                            )
                        )
                        appState?.navController?.navigateUp()
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