package com.edgetech.bbscout.features.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.R
import com.edgetech.bbscout.components.ui.StatusDialog
import com.edgetech.bbscout.components.ui.WarningIcon
import com.edgetech.bbscout.data.utils.DataConstants
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.auth.domain.model.AuthUiEvent
import com.edgetech.bbscout.features.auth.domain.model.AuthUiModel
import com.edgetech.bbscout.features.auth.domain.viewmodel.AuthViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


@Composable
fun SettingsScreen(
    authViewModel: AuthViewmodel,
    appState: BBScoutAppState?,
){
    SettingsMain(
        authUiModel = authViewModel.uiModel,
        appState = appState
    )
}

@Composable
fun SettingsMain(
    authUiModel: AuthUiModel,
    appState: BBScoutAppState?
){

    val authUiState by authUiModel.authUiState.collectAsState()
    val authUiEvent by authUiModel.authUiEvent.collectAsState()
    val authEventSink = authUiModel.authEventSink

    val user = authUiState.user

    val context = LocalContext.current

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
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(DataConstants.GOOGLE_CLIENT_ID) // Replace with your client ID
                    .requestEmail()
                    .build()

                val client = GoogleSignIn.getClient(context, gso)
                client.signOut()
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
        showLogOutWarning = false
        appState?.navController?.navigate(AppDestinations.Login)
        authEventSink(AuthEventSink.ResetState)
    } else if (authUiEvent is AuthUiEvent.Error){
        authEventSink(AuthEventSink.ResetState)
    }

    Scaffold {
        Column(
            modifier = Modifier.padding(it).padding(16.dp),
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

            Text(text = "Policies", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp).align(Alignment.Start))
            Card(
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                SettingsItemComp(
                    "Privacy Policy",
                    R.drawable.privacy_dashboard_svgrepo_com,
                    onClick = {

                    }

                )
                SettingsItemComp(
                    "Terms and conditions",
                    R.drawable.terms_svgrepo_com,
                    onClick = {

                    },
                    hasDivider = false

                )
            }

            Text(text = "Account", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp).align(Alignment.Start))
            Card(
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                SettingsItemComp(
                    "Change Password",
                    R.drawable.password_minimalistic_input_svgrepo_com,
                    onClick = {
                        appState?.navController?.navigate(AppDestinations.ChangePassword)
                    }

                )
                SettingsItemComp(
                    "Logout",
                    R.drawable.logout_svgrepo_com,
                    onClick = {
                        showLogOutWarning = true
                    },
                    hasDivider = false

                )
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