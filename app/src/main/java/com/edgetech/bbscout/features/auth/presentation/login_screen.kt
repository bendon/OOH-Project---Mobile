package com.edgetech.bbscout.features.auth.presentation


import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.edgetech.bbscout.R
import com.edgetech.bbscout.components.ui.ButtonContent
import com.edgetech.bbscout.components.ui.ErrorShowDialog
import com.edgetech.bbscout.components.ui.MainLoadingButton
import com.edgetech.bbscout.components.ui.NonLoadingSecButton
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.BadRequestException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.UnAuthenticatedException
import com.edgetech.bbscout.data.utils.DataConstants
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.auth.domain.model.AuthUiEvent
import com.edgetech.bbscout.features.auth.domain.model.AuthUiModel
import com.edgetech.bbscout.features.auth.domain.model.EmptyCredentialsException
import com.edgetech.bbscout.features.auth.domain.model.EmptyNameException
import com.edgetech.bbscout.features.auth.domain.model.PasswordDoNotMatchException
import com.edgetech.bbscout.features.auth.domain.model.RegistrationBadRequest
import com.edgetech.bbscout.features.auth.domain.viewmodel.AuthViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@Composable
fun LoginScreen(
    authViewmodel: AuthViewmodel,
    navController: NavController
) {
    LoginScreenMain(
        authUiModel = authViewmodel.uiModel,
        navController = navController
    )
}


@Composable
fun LoginScreenMain(
    authUiModel: AuthUiModel,
    navController: NavController
) {

    val activity = LocalActivity.current
    BackHandler {
        activity?.finish()
    }

    val uiState by authUiModel.authUiState.collectAsState()
    val uiEvent by authUiModel.authUiEvent.collectAsState()


    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Login", "Sign Up")
    var token by rememberSaveable { mutableStateOf<String?>(null) }

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
                if (selectedTab == 0) {
                    authUiModel.authEventSink(
                        AuthEventSink.LoginWithGoogle(idToken)
                    )
                } else {
                    authUiModel.authEventSink(
                        AuthEventSink.RegisterWithGoogle(idToken)
                    )
                }
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
    } else if (uiEvent is AuthUiEvent.RegistrationSuccessful) {
        navController.navigate(AppDestinations.Dashboard)
        authUiModel.authEventSink(
            AuthEventSink.ResetState
        )
    } else if (uiEvent is AuthUiEvent.Error) {
        val request = (uiEvent as AuthUiEvent.Error)
        ErrorShowDialog(
            showErrorMessage = true,
            customError = mapOf(
                UnAuthenticatedException() to "Wrong credentials",
                EmptyCredentialsException to "Please provide an email and password",
                PasswordDoNotMatchException to "Passwords can not be empty and have to match",
                EmptyNameException to "Please provide a first and last name",
                RegistrationBadRequest to "Bad request, it is possible that you are trying to use an email or phone number that is already in use by another account."
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

    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp)
                .imePadding(),
        ) {

            Image(
                painter = painterResource(R.drawable.bbscout),
                null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 24.dp)
            )
            Text(
                text = "Welcome to BBScout",
                fontSize = 24.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "The smart way to track billboard advertising",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                LoginScreenContent(
                    authUiModel, launcher, navController
                )
            } else {
                SignUpScreen(authUiModel, launcher)
            }

        }
    }

}


@Composable
fun LoginScreenContent(
    authUiModel: AuthUiModel,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    navController: NavController
) {
    var email = rememberTextFieldState()
    var password = rememberTextFieldState()

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val uiState by authUiModel.authUiState.collectAsState()
    val isLoading = uiState.isLoading
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Email Address", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start)
        )
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Password", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start)
        )
        OutlinedTextField(
            state = password,
            placeholder = { Text("Enter your password") },
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
        Text(
            "Forgot password?",
            modifier = Modifier
                .clickable {
                    navController.navigate(AppDestinations.ForgotPassword)
                }
                .padding(vertical = 8.dp)
                .align(Alignment.End),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
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
                    AuthEventSink.LoginWithEmailAndPassword(
                        email.text.toString(),
                        password.text.toString()
                    )
                )
            },
        ) {
            ButtonContent(
                "Login",
                Color.White,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Or continue with", modifier = Modifier
                    .padding(horizontal = 8.dp)
            )
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
        }
        NonLoadingSecButton(
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
            Image(
                painter = painterResource(R.drawable.google_icon_logo_svgrepo_com),
                contentDescription = null,
                //colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(32.dp).padding(vertical = 8.dp)
            )
            Text(
                "Login with Google", color =
                MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Stable
class PasswordTransformation : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {

        // "•".repeat(length)
        if (length > 0)
            replace(0, length, "•".repeat(length))

    }
}

@Composable
fun SignUpScreen(
    authUiModel: AuthUiModel,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {

    var firstName = rememberTextFieldState()
    var lastName = rememberTextFieldState()
    var email = rememberTextFieldState()
    var password = rememberTextFieldState()
    var confirmPassword = rememberTextFieldState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val uiState by authUiModel.authUiState.collectAsState()
    val isLoading = uiState.isLoading
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.verticalScroll(
            rememberScrollState()
        )
    ) {

        Text(
            "First name", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start)
        )
        OutlinedTextField(
            state = firstName,
            placeholder = { Text("Enter your first name") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentPadding = PaddingValues(14.dp),
            leadingIcon = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Last name", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start)
        )
        OutlinedTextField(
            state = lastName,
            placeholder = { Text("Enter your last name") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentPadding = PaddingValues(14.dp),
            leadingIcon = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Email Address", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start)
        )
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Password", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start)
        )
        OutlinedTextField(
            state = password,
            placeholder = { Text("Enter your password") },
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
        Text(
            "Confirm Password", modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.Start)
        )
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
                    AuthEventSink.RegisterWithEmailAndPassword(
                        firstName = firstName.text.toString(),
                        lastName = lastName.text.toString(),
                        email = email.text.toString(),
                        password = password.text.toString(),
                        passwordConfirmation = confirmPassword.text.toString()
                    )
                )
            },
        ) {
            ButtonContent(
                "Create an account",
                Color.White,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Or continue with", modifier = Modifier
                    .padding(horizontal = 8.dp)
            )
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
        }
        NonLoadingSecButton(
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
            Image(
                painter = painterResource(R.drawable.google_icon_logo_svgrepo_com),
                contentDescription = null,
                //colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(32.dp).padding(vertical = 8.dp)
            )
            Text(
                "Create an account with Google", color =
                MaterialTheme.colorScheme.primary
            )


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

@Preview
@Composable
fun AuthScreenPreview(

) {

}

