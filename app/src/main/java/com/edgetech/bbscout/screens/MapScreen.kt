package com.edgetech.bbscout.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// Updated Billboard Data Class with status
data class BillboardInfo(
    val id: String,
    val location: LatLng,
    val name: String,
    val address: String,
    val size: String,
    val type: String,
    val currentAdvertisement: String? = null,
    val status: BillboardStatus
)

// Enum to represent billboard status
enum class BillboardStatus {
    AVAILABLE,   // Green
    RESERVED,    // Orange
    BOOKED       // Red
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasLocationPermission = isGranted
    }

    var selectedBillboard by remember { mutableStateOf<BillboardInfo?>(null) }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val nairobi = LatLng(-1.286389, 36.817223)

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(nairobi, 15f)
        }

        val billboards = remember {
            listOf(
                BillboardInfo(
                    id = "1",
                    location = LatLng(-1.286389, 36.817223),
                    name = "Uhuru Highway Billboard",
                    address = "Uhuru Highway, Near Maasai Market",
                    size = "14m x 6m",
                    type = "Digital",
                    currentAdvertisement = "Coca-Cola Summer Campaign",
                    status = BillboardStatus.BOOKED
                ),
                BillboardInfo(
                    id = "2",
                    location = LatLng(-1.285389, 36.816223),
                    name = "Jamia Mosque Billboard",
                    address = "Uhuru Highway, Near Jamia Mosque",
                    size = "12m x 4m",
                    type = "Static",
                    currentAdvertisement = null,
                    status = BillboardStatus.AVAILABLE
                ),
                BillboardInfo(
                    id = "3",
                    location = LatLng(-1.287389, 36.818223),
                    name = "Kenyatta Avenue Billboard",
                    address = "Kenyatta Avenue Junction",
                    size = "10m x 5m",
                    type = "Digital",
                    currentAdvertisement = "Kenya Airways New Routes",
                    status = BillboardStatus.RESERVED
                )
            )
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            )
        ) {
            billboards.forEach { billboard ->
                Marker(
                    state = MarkerState(position = billboard.location),
                    title = billboard.name,
                    icon = when (billboard.status) {
                        BillboardStatus.AVAILABLE -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        BillboardStatus.RESERVED -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        BillboardStatus.BOOKED -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    },
                    onClick = {
                        selectedBillboard = billboard
                        true
                    }
                )
            }
        }

        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Search for billboards...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (selectedBillboard != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedBillboard = null },
                modifier = Modifier.fillMaxHeight(0.5f),
                dragHandle = {
                    Surface(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ) { }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedBillboard?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedBillboard = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    BillboardDetailItem("Address", selectedBillboard?.address ?: "")
                    BillboardDetailItem("Size", selectedBillboard?.size ?: "")
                    BillboardDetailItem("Type", selectedBillboard?.type ?: "")
                    BillboardDetailItem(
                        "Current Advertisement",
                        selectedBillboard?.currentAdvertisement ?: "Available"
                    )
                    BillboardDetailItem(
                        "Status",
                        when (selectedBillboard?.status) {
                            BillboardStatus.AVAILABLE -> "Available for Advertising"
                            BillboardStatus.RESERVED -> "Reserved"
                            BillboardStatus.BOOKED -> "Booked"
                            null -> "Unknown"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BillboardDetailItem(label: String, value: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}