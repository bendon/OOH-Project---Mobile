package com.edgetech.bbscout.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.capture.presentation.capture_listing.BillboardListingItem
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.edgetech.bbscout.features.navigation.DashboardScreenOption
import com.edgetech.bbscout.ui.theme.BBScoutTheme


@Composable
fun HomeDashboard(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel = hiltViewModel(),
    onPageTap: (DashboardScreenOption) -> Unit = {},
){
    HomeDashboard(
        appState = appState,
        captureRecordUiModel = captureRecordViewmodel.uiModel,
        onPageTap = onPageTap
    )
}


@Composable
fun HomeDashboard(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel,
    onPageTap: (DashboardScreenOption) -> Unit = {},
) {

    val capturesUiState by captureRecordUiModel.captureUiState.collectAsState()

    val recentEntries = capturesUiState.allCaptures.take(5)

    LaunchedEffect(true) {
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.OnGetAllCaptures
        )
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.GetRecentCaptures
        )
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())

        ) {
            HeaderSectionI()
            HeaderSection(numberOfCaptures = capturesUiState.allCaptures.size)
            Spacer(modifier = Modifier.height(16.dp))
            ChallengeCard()
            Spacer(modifier = Modifier.height(16.dp))
            QuickActions(appState, onPageTap = onPageTap)
//            Spacer(modifier = Modifier.height(16.dp))
//            NearbyBillboards()
            Spacer(modifier = Modifier.height(16.dp))
            RecentActivity(recentEntries, appState)
        }
    }


@Composable
fun HeaderSectionI() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Billboard Hunter", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Welcome back", fontSize = 14.sp, color = Color.Gray)
        }
        IconButton(onClick = { /* TODO: Handle notification */ }) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
        }
    }
}

@Composable
fun HeaderSection(
    modifier: Modifier = Modifier.padding(top = 32.dp),
    numberOfCaptures: Int,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "${numberOfCaptures} Captures", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        //Text(text = "96% Accuracy", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
       // Text(text = "Level 12", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChallengeCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            Column(modifier = Modifier
                .padding(20.dp)
                .weight(1f)) {
                Text(
                    text = "New Challenge Available!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Map your first 5 billboards.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                //  TextButton(onClick = {}) {
//                Text(
//                    "View Challenge",
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = MaterialTheme.colorScheme.primary
//                )
                // }
            }
            //Icon(Icons.Outlined.WarningAmber, contentDescription = null)
        }
    }
}

@Composable
fun QuickActions(appState: BBScoutAppState?,onPageTap: (DashboardScreenOption) -> Unit = {},) {
    Column {
        Text(text = "Quick action", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onPageTap(DashboardScreenOption.CAPTURE)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Capture")
            }
            OutlinedButton(
                onClick = {
                    onPageTap(DashboardScreenOption.HISTORY)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search Area")
            }
        }
    }
}

@Composable
fun NearbyBillboards() {
    Column {
        Text(text = "Nearby Billboards", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        BillboardItem("Main Street Billboard", "0.3 km away • Unverified")
        BillboardItem("Central Park Display", "0.7 km away • Digital")
    }
}

@Composable
fun BillboardItem(name: String, details: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp),
                ){
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier
                    .size(32.dp)
                    .padding(8.dp))
            }
            Column(modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)) {
                Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = details, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RecentActivity(recentCaptures: List<EntryRecord>, appState: BBScoutAppState?) {
    Column {
        Text(text = "Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        recentCaptures.forEach { item ->
            BillboardListingItem(
                item,
                onTap = {
                    appState?.navController?.navigate(AppDestinations.CaptureDetail(item.entryEntity.id))
                }
            )
        }

    }
}

@Preview
@Composable
fun AndroidDashboardPreview() {
    BBScoutTheme{
        HomeDashboard(null)
    }
}