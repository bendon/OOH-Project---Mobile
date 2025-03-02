package com.edgetech.bbscout.features.auth.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.ButtonContent
import com.edgetech.bbscout.components.ui.ErrorShowDialog
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.ui.StatusDialog
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.UnAuthenticatedException
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.auth.domain.model.AuthUiEvent
import com.edgetech.bbscout.features.auth.domain.model.AuthUiModel
import com.edgetech.bbscout.features.auth.domain.model.EmptyCredentialsException
import com.edgetech.bbscout.features.auth.domain.model.EmptyNameException
import com.edgetech.bbscout.features.auth.domain.model.PasswordDoNotMatchException
import com.edgetech.bbscout.features.auth.domain.model.RegistrationBadRequest
import com.edgetech.bbscout.features.auth.domain.model.RequestPasswordResetBadRequest
import com.edgetech.bbscout.features.auth.domain.viewmodel.AuthViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun RequestPasswordScreen(
    authViewmodel: AuthViewmodel,
    appState: BBScoutAppState?,
){
    RequestPasswordMain(
        authUiModel = authViewmodel.uiModel,
        appState = appState
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestPasswordMain(
    authUiModel: AuthUiModel,
    appState: BBScoutAppState?,
){

    var email = rememberTextFieldState()
    val uiState by authUiModel.authUiState.collectAsState()
    val isLoading = uiState.isLoading
    val uiEvent by authUiModel.authUiEvent.collectAsState()


    if (uiEvent is AuthUiEvent.RegistrationSuccessful){
        StatusDialog(
            true,
            onDismissRequest = {
                appState?.navController?.navigateUp()
            },
            title = "Password reset",
            message = "A new password has been sent to your email address, please change it after logging in",
            negText = "Close"
        )
    }
    else if (uiEvent is AuthUiEvent.Error) {
        val request = (uiEvent as AuthUiEvent.Error)
        ErrorShowDialog(
            showErrorMessage = true,
            customError = mapOf(
                EmptyCredentialsException to "Please provide an email address",
                RequestPasswordResetBadRequest to "Bad request, it is possible that you are trying to use an email or phone number that does not exist."
            ),
            error = request.exception,
            event = request.eventSink,
            onDismiss = {
                authUiModel.authEventSink(
                    AuthEventSink.ResetState
                )
            },
            onPositive = { eventSink, ex ->
                if (ex !is EmptyCredentialsException) {
                    authUiModel.authEventSink(eventSink as AuthEventSink)
                } else {
                    authUiModel.authEventSink(
                        AuthEventSink.ResetState
                    )
                }
            }

        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                    .copy(containerColor = MaterialTheme.colorScheme.background, navigationIconContentColor = MaterialTheme.colorScheme.onError, titleContentColor = MaterialTheme.colorScheme.onBackground),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(text = "Request password") },
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
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp).imePadding(),
        ) {
            Text("Email Address", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start))
            OutlinedTextField(
                state = email,
                placeholder = { Text("Enter your email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentPadding = PaddingValues(14.dp),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.AlternateEmail,
                        contentDescription = null,
                    )
                }
            )
            Spacer(
                modifier = Modifier.weight(1f)
            )
            MainLoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                pIsLoading = isLoading,
                disableOnTap = false,
                loadOnTap = false,
                onTap = {
                    authUiModel.authEventSink(
                            AuthEventSink.RequestPasswordReset(
                                email.toString()
                            )
                    )
                },
            ) {
                ButtonContent(
                    "Request new password",
                    Color.White,
                )
            }

        }

    }

}