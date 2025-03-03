package com.edgetech.bbscout.features.capture.presentation.review_data

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.setTextAndSelectAll
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.navOptions
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.location.GetLocationInfo
import com.edgetech.bbscout.components.ui.LargeDropdownMenu
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


    var campaignBrand = rememberTextFieldState()
    var campaignDescription = rememberTextFieldState()


    var billboardType by rememberSaveable {
        mutableStateOf("")
    }

    var billboardOwner = rememberTextFieldState()

    var billboardWidth = rememberTextFieldState()

    var billboardLength = rememberTextFieldState()

    val billboardTypes = listOf(
        "Static Billboard",
        "Digital Billboard",
        "Banner Ads",
        "Wallscapes",
        "Mobile Billboards",
        "Lamp Posts",
        "Interactive Billboards"
    )

    val unitOfMeasurements = listOf("centimeters", "meters", "feet", "inches")

    var selectedUnitOfMeasurement by rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(
        recordId
    ) {
        if (recordId == null) {
            campaignBrand.setTextAndPlaceCursorAtEnd(currentData?.brandName?.ifEmptySetNull() ?: "")
            campaignDescription.setTextAndPlaceCursorAtEnd(
                currentData?.brandCampaign.ifEmptySetNull() ?: ""
            )
            billboardType = currentData?.billboardType.ifEmptySetNull() ?: ""
            billboardOwner.setTextAndPlaceCursorAtEnd(
                currentData?.billboardOwner.ifEmptySetNull() ?: ""
            )
            billboardWidth.setTextAndPlaceCursorAtEnd(
                currentData?.billboardWidth.ifEmptySetNull() ?: ""
            )
            billboardLength.setTextAndPlaceCursorAtEnd(
                currentData?.billboardLength.ifEmptySetNull() ?: ""
            )
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
                    Text(
                        "Brand name",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(bottom = 2.dp, top = 8.dp)
                            .align(Alignment.Start)
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        contentPadding = PaddingValues(14.dp),
                        state = campaignBrand,
                        placeholder = { Text("Campaign brand") }
                    )
                    Text(
                        "Campaign description",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(bottom = 2.dp, top = 8.dp)
                            .align(Alignment.Start)
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        contentPadding = PaddingValues(14.dp),
                        state = campaignDescription,
                        placeholder = { Text("Campaign description") })
                }
                if (recordType == RecordType.BILLBOARD_INFO) {
                    Text(
                        text = "Billboard information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        "Billboard owner",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(bottom = 2.dp, top = 8.dp)
                            .align(Alignment.Start)
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        contentPadding = PaddingValues(14.dp),
                        state = billboardOwner,
                        placeholder = { Text("Billboard owner") })
                    LargeDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = "Billboard type",
                        items = billboardTypes,
                        selectedIndex = billboardTypes.indexOf(billboardType),
                        onItemSelected = { index, item ->
                            billboardType = item
                        },

                        )


                    Row(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 2.dp)
                        ) {
                            Text(
                                "Width",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(bottom = 2.dp, top = 8.dp)
                                    .align(Alignment.Start)
                            )
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                contentPadding = PaddingValues(14.dp),
                                state = billboardWidth,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("Width") })
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 2.dp)
                        ) {
                            Text(
                                "Height",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(bottom = 2.dp, top = 8.dp)
                                    .align(Alignment.Start)
                            )
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                contentPadding = PaddingValues(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                state = billboardLength,
                                placeholder = { Text("Height") })
                        }
                    }

                    LargeDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = "Unit of measurement",
                        items = unitOfMeasurements,
                        selectedIndex = unitOfMeasurements.indexOf(selectedUnitOfMeasurement),
                        onItemSelected = { index, item ->
                            selectedUnitOfMeasurement = item
                        },

                        )
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
                            CaptureRecordEventSink.OnEditCaptureEvent(
                                billboardData = currentData?.copy(
                                    brandName = campaignBrand.text.toString().ifEmptySetNull()
                                        ?: currentData.brandName,
                                    brandCampaign = campaignDescription.text.toString()
                                        .ifEmptySetNull() ?: currentData.brandCampaign,
                                    billboardType = billboardType.ifEmptySetNull()
                                        ?: currentData.billboardType,
                                    billboardOwner = billboardOwner.text.toString().ifEmptySetNull()
                                        ?: currentData.billboardOwner,
                                    billboardWidth = billboardWidth.text.toString().ifEmptySetNull()
                                        ?: currentData.billboardWidth,
                                    billboardLength = billboardLength.text.toString()
                                        .ifEmptySetNull() ?: currentData.billboardLength,
                                    unitOfMeasurement = selectedUnitOfMeasurement.ifEmptySetNull()
                                        ?: currentData.unitOfMeasurement
                                ) ?: BillboardExtractedInfo(
                                    brandName = campaignBrand.text.toString().ifEmptySetNull()
                                        ?: currentData?.brandName,
                                    brandCampaign = campaignDescription.text.toString()
                                        .ifEmptySetNull() ?: currentData?.brandCampaign,
                                    billboardType = billboardType.ifEmptySetNull()
                                        ?: currentData?.billboardType,
                                    billboardOwner = billboardOwner.text.toString().ifEmptySetNull()
                                        ?: currentData?.billboardOwner,
                                    billboardWidth = billboardWidth.text.toString().ifEmptySetNull()
                                        ?: currentData?.billboardWidth,
                                    billboardLength = billboardLength.text.toString()
                                        .ifEmptySetNull() ?: currentData?.billboardLength,
                                    unitOfMeasurement = selectedUnitOfMeasurement.ifEmptySetNull()
                                        ?: currentData?.unitOfMeasurement
                                )
                            )
                        )
                        appState?.navController?.navigateUp()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
//                    Icon(Icons.Default.CheckCircleOutline, contentDescription = "Capture")
//                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }


            }

        }

    }


}