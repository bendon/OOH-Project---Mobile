package com.edgetech.bbscout.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.features.navigation.BBScoutDashboardNavigation
import com.edgetech.bbscout.features.navigation.DashboardScreenOption
import com.edgetech.bbscout.ui.theme.BBScoutTheme


@Composable
fun BBScoutDashboard(
    appState: BBScoutAppState?
){

    var selectedPage by rememberSaveable {
        mutableStateOf<DashboardScreenOption>(DashboardScreenOption.HOME)
    }


    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background
            ) {
                DashboardScreenOption.entries.forEach { screen ->
                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.background, selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary),
                        icon = { Icon(if(screen == selectedPage) screen.iconSelected else screen.iconUnselected, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = screen == selectedPage,
                        onClick = {
                            selectedPage = screen
                            appState?.dashboardNavController?.navigate(screen.name)
                        }
                    )
                }
            }
        }
    ) {
        BBScoutDashboardNavigation(
            appState = appState,
            modifier = Modifier.padding(it)
        )
    }

}

