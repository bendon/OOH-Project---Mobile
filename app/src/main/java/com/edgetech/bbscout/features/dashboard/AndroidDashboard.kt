package com.edgetech.bbscout.features.dashboard

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.utils.defaultZoneId
import com.edgetech.bbscout.components.utils.getFullDateAndTimeFromLong
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.capture.presentation.capture_listing.BillboardListingItem
import com.edgetech.bbscout.features.capture_start.checkCameraPermission
import com.edgetech.bbscout.features.capture_start.checkLocationPermission
import com.edgetech.bbscout.features.capture_start.isGPSEnabled
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.edgetech.bbscout.features.navigation.DashboardScreenOption
import com.edgetech.bbscout.ui.theme.BBScoutTheme


@Composable
fun HomeDashboard(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel = hiltViewModel(),
    onPageTap: (DashboardScreenOption) -> Unit = {},
) {
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

    Column {
        if (capturesUiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())

        ) {

            HeaderSectionI()
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.surface)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                HeaderSection(numberOfCaptures = capturesUiState.allCaptures.size)
                Spacer(modifier = Modifier.height(16.dp))
                ChallengeCard(appState)
                Spacer(modifier = Modifier.height(16.dp))
                QuickActions(appState, onPageTap = onPageTap)
                //            Spacer(modifier = Modifier.height(16.dp))
                //            NearbyBillboards()
                Spacer(modifier = Modifier.height(16.dp))
                if (recentEntries.isNotEmpty())
                    RecentActivity(recentEntries, appState)
            }

        }
    }
}


@Composable
fun HeaderSectionI() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HeaderSection(
    modifier: Modifier = Modifier.padding(top = 16.dp),
    numberOfCaptures: Int,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${numberOfCaptures}",
            style = MaterialTheme.typography.headlineLargeEmphasized,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Captures", fontSize = 14.sp)

        //Text(text = "96% Accuracy", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
        // Text(text = "Level 12", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChallengeCard(
    appState: BBScoutAppState?
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = "New Challenge Available!",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Map your next billboards.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Start challenge",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp).clickable {
                        navigateToCapture(appState, context)
                    }
                )
            }
            //Icon(Icons.Outlined.WarningAmber, contentDescription = null)
        }
    }
}

fun navigateToCapture(
    appState: BBScoutAppState?,
    context: Context
){
    if (checkCameraPermission(context) && checkLocationPermission(context) && isGPSEnabled(
            context
        )
    )
        appState?.navController?.navigate(AppDestinations.CameraCapture)
    else
        appState?.navController?.navigate(AppDestinations.CaptureCheckRequirement)
}

@Composable
fun QuickActions(appState: BBScoutAppState?, onPageTap: (DashboardScreenOption) -> Unit = {}) {

    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(text = "Quick action", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                onClick = {
                    navigateToCapture(appState, context)
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = "Capture",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "New Capture",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Card(
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                onClick = {
                    onPageTap(DashboardScreenOption.HISTORY)
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "Search Area",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
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
fun BillboardItem(
    name: String,
    details: String,
    modifier: Modifier = Modifier,
    hasDivider: Boolean = true
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(8.dp)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = details,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                Icons.Outlined.ArrowForwardIos,
                null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
    if (hasDivider)
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.surface)
}

@Composable
fun RecentActivity(recentCaptures: List<EntryRecord>, appState: BBScoutAppState?) {
    Column {
        Text(text = "Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Card(
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            recentCaptures.forEachIndexed { index, item ->
                BillboardItem(
                    item.entryEntity.brand ?: "N/A",
                    "${item.location?.locationName ?: ""} ${if (!item.location?.locationName.isNullOrEmpty()) "•" else ""} ${
                        item.entryEntity.createdAt?.getFullDateAndTimeFromLong(
                            defaultZoneId.id
                        ) ?: ""
                    }",
                    hasDivider = index != recentCaptures.lastIndex,
                    modifier = Modifier.clickable {
                        appState?.navController?.navigate(AppDestinations.CaptureDetail(item.entryEntity.remoteId ?: ""))
                    }
                    )

            }

        }
    }
}

@Preview
@Composable
fun AndroidDashboardPreview() {
    BBScoutTheme {
        HomeDashboard(null)
    }
}