package com.edgetech.bbscout.features.auth.presentation


import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.edgetech.bbscout.R
import com.edgetech.bbscout.components.ui.ButtonContent
import com.edgetech.bbscout.components.ui.ErrorShowDialog
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.ui.NonLoadingSecButton
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.UnAuthenticatedException
import com.edgetech.bbscout.data.utils.DataConstants
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.auth.domain.model.AuthUiEvent
import com.edgetech.bbscout.features.auth.domain.model.AuthUiModel
import com.edgetech.bbscout.features.auth.domain.model.EmptyCredentialsException
import com.edgetech.bbscout.features.auth.domain.viewmodel.AuthViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@Composable
fun LoginScreen(
    authViewmodel: AuthViewmodel,
    navController: NavController
){
    LoginScreenMain(
        authUiModel = authViewmodel.uiModel,
        navController = navController
    )
}


@Composable
fun LoginScreenMain(
    authUiModel: AuthUiModel,
    navController: NavController
){

    val activity = LocalActivity.current
    BackHandler {
        activity?.finish()
    }

    var credentialAddress by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val uiState by authUiModel.authUiState.collectAsState()
    val uiEvent by authUiModel.authUiEvent.collectAsState()

    val isLoading = uiState.isLoading

    val context = LocalContext.current
    var token by rememberSaveable  { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            // Get the ID token for backend authentication
            val idToken = account?.idToken
            if (idToken != null) {
                token = idToken
                authUiModel.authEventSink(
                    AuthEventSink.LoginWithGoogle(idToken)
                )
            } else {

            }
        } catch (e: ApiException) {

        }
    }

    if (uiEvent is AuthUiEvent.LoginSuccessful) {
        navController.navigate(AppDestinations.Dashboard)
        authUiModel.authEventSink(
            AuthEventSink.ResetState
        )
    } else if (uiEvent is AuthUiEvent.Error){
        val request = (uiEvent as AuthUiEvent.Error)
        ErrorShowDialog(
            showErrorMessage = true,
            customError = mapOf(UnAuthenticatedException() to "Wrong credentials",
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
                if (ex !is EmptyCredentialsException && eventSink != null){
                    authUiModel.authEventSink(eventSink as AuthEventSink)
                } else {
                    authUiModel.authEventSink(
                        AuthEventSink.ResetState
                    )
                }
            }

        )
    }

    Scaffold {
        Column(
            modifier = Modifier.padding(it).padding(horizontal = 16.dp).imePadding(),
        ) {

            Image(
                painter = painterResource(R.drawable.bbscout),
                null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 24.dp)
            )
            Text(text = "Welcome to BBScout!", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 16.dp))
            Text(text = "Login", style = MaterialTheme.typography.headlineLarge,)
            OutlinedTextField(
                value = credentialAddress,
                keyboardOptions =  KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                onValueChange = { credentialAddress = it },
                label = {
                    Text(
                        "Email Address"
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description ="Toggle password visibility"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )

            MainLoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                pIsLoading = isLoading,
                disableOnTap = false,
                loadOnTap = false,
                onTap = {
                        authUiModel.authEventSink(
                            AuthEventSink.LoginWithEmailAndPassword(
                                credentialAddress,
                                password
                            )
                        )
                },
            ) {
                ButtonContent(
                    "Login",
                    Color.White,
                )
            }

            Text("Or", modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp))
            NonLoadingSecButton (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                disableOnTap = false,
                onTap = {
                    launchGoogleSignIn(
                        context,
                        launcher
                    )
                },
            ) {
                ButtonContent(
                    "Login with Google",
                    MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

}

private fun launchGoogleSignIn(
    context: Context,
    launcher: ActivityResultLauncher<Intent>
) {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(DataConstants.GOOGLE_CLIENT_ID) // Replace with your client ID
        .requestEmail()
        .build()

    val client = GoogleSignIn.getClient(context, gso)
    val signInIntent = client.signInIntent
    launcher.launch(signInIntent)
}

