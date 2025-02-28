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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.ButtonContent
import com.edgetech.bbscout.components.ui.ErrorShowDialog
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.UnAuthenticatedException
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.auth.domain.model.AuthUiEvent
import com.edgetech.bbscout.features.auth.domain.model.AuthUiModel
import com.edgetech.bbscout.features.auth.domain.model.EmptyCredentialsException
import com.edgetech.bbscout.features.auth.domain.viewmodel.AuthViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun ChangePasswordScreen(
    appState: BBScoutAppState?,
    authViewmodel: AuthViewmodel
){
    ChangePasswordMain(
        appState = appState,
        authUiModel = authViewmodel.uiModel
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordMain(
    appState: BBScoutAppState?,
    authUiModel: AuthUiModel
){
    var previousPassword = rememberTextFieldState()
    var password = rememberTextFieldState()
    var confirmPassword = rememberTextFieldState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val uiState by authUiModel.authUiState.collectAsState()
    val isLoading = uiState.isLoading
    val uiEvent by authUiModel.authUiEvent.collectAsState()
    if (uiEvent is AuthUiEvent.PasswordChangeSuccessful) {
        appState?.navController?.navigateUp()
        authUiModel.authEventSink(
            AuthEventSink.ResetState
        )
    } else if (uiEvent is AuthUiEvent.Error) {
        val request = (uiEvent as AuthUiEvent.Error)
        ErrorShowDialog(
            showErrorMessage = true,
            customError = mapOf(
                UnAuthenticatedException() to "Wrong credentials",
                EmptyCredentialsException to "Please provide an email and password"
            ),
            error = request.exception,
            event = request.eventSink,
            onDismiss = {
                authUiModel.authEventSink(
                    AuthEventSink.ResetState
                )
            },
            onPositive = { eventSink, ex ->
                if (ex !is EmptyCredentialsException && eventSink != null) {
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
                    .copy(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(text = "Change password") },
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
                .imePadding(),
        ) {
            Spacer(
                modifier = Modifier.height(24.dp)
            )
            Text("Previous password", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start))
            OutlinedTextField(
                state = previousPassword,
                placeholder = { Text("Enter your previous password") },
                outputTransformation = if (passwordVisible) null else PasswordTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentPadding = PaddingValues(14.dp),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null
                    )
                }
            )


            Spacer(modifier = Modifier.height(8.dp))
            Text("New password", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start))
            OutlinedTextField(
                state = password,
                placeholder = { Text("Enter your new password") },
                outputTransformation = if (passwordVisible) null else PasswordTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentPadding = PaddingValues(14.dp),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null
                    )
                }
            )


            Spacer(modifier = Modifier.height(8.dp))
            Text("Confirm Password", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start))
            OutlinedTextField(
                state = confirmPassword,
                placeholder = { Text("Confirm your password") },
                outputTransformation = if (passwordVisible) null else PasswordTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentPadding = PaddingValues(14.dp),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null
                    )
                }
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
                        AuthEventSink.ChangePassword(
                            previousPassword.text.toString(),
                            password.text.toString(),
                            confirmPassword.text.toString()
                        )
                    )
                },
            ) {
                ButtonContent(
                    "Change password",
                    MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }

}