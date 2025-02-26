package com.edgetech.bbscout.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.edgetech.bbscout.components.ui.StatusDialog
import com.edgetech.bbscout.components.ui.WarningIcon
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.auth.domain.model.AuthUiEvent
import com.edgetech.bbscout.features.auth.domain.model.AuthUiModel
import com.edgetech.bbscout.features.auth.domain.viewmodel.AuthViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun SettingsScreen(
    authViewModel: AuthViewmodel,
    navController: NavController
){
    SettingsMain(
        authUiModel = authViewModel.uiModel,
        navController = navController
    )
}

@Composable
fun SettingsMain(
    authUiModel: AuthUiModel,
    navController: NavController
){

    val authUiState by authUiModel.authUiState.collectAsState()
    val authUiEvent by authUiModel.authUiEvent.collectAsState()
    val authEventSink = authUiModel.authEventSink

    val user = authUiState.user

    var showLogOutWarning by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(true) {
        authEventSink(AuthEventSink.GetAccountInfo)
    }

    if (showLogOutWarning){
        StatusDialog(
            isVisible = true,
            onDismissRequest = {
                showLogOutWarning = false
            },
            onConfirmation = {
                showLogOutWarning = false
                authEventSink(AuthEventSink.Logout)
            },
            title = "Log Out",
            message = "Are you sure you want to log out?",
            posText = "Log Out",
            negText = "Cancel",
            icon = {
                WarningIcon()
            }
        )
    }

    if (authUiEvent is AuthUiEvent.LogoutSuccessful){
        navController.navigate(AppDestinations.Login)
    } else if (authUiEvent is AuthUiEvent.Error){

    }

    Scaffold {
        Column(
            modifier = Modifier.padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(124.dp).padding(vertical = 16.dp, horizontal = 16.dp),

            ) {
                Icon(
                    Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
            Text("${user?.firstName} ${user?.lastName}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            SettingsItem(
                Icons.Outlined.Logout,
                "Logout",
                modifier = Modifier.padding(vertical = 16.dp)
            ){
                showLogOutWarning = true
            }




        }
    }

}

@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onTab: () -> Unit,
){
    Row(
        modifier = modifier.clickable { onTab() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 16.dp).weight(1f)
        )
    }
}