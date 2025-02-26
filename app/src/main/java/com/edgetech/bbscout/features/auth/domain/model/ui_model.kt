package com.edgetech.bbscout.features.auth.domain.model

import com.edgetech.bbscout.components.domain_util.AppEventSink
import com.edgetech.bbscout.data.utils.BBScoutException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AuthUiModel(
    val authUiState: StateFlow<AuthUiState> = MutableStateFlow(AuthUiState()),
    val authUiEvent: StateFlow<AuthUiEvent> = MutableStateFlow(AuthUiEvent.Empty),
    val authEventSink: (AuthEventSink) -> Unit = {}
)


data class AuthUiState(
    val isLoading: Boolean = false,
)

sealed class AuthUiEvent {
    data class LoginSuccessful(val token: String?) : AuthUiEvent()

    data class Error(val exception: BBScoutException, val eventSink : AppEventSink) : AuthUiEvent()

    data object Empty : AuthUiEvent()

    data object LogoutSuccessful : AuthUiEvent()

    data object PasswordChangeSuccessful : AuthUiEvent()

}


sealed class AuthEventSink : AppEventSink {

    data class LoginWithEmailAndPassword(
        val email: String,
        val password: String,
    ) : AuthEventSink()


    data class LoginWithGoogle(
        val token: String
    ) : AuthEventSink()

    data object Logout : AuthEventSink()

    data object GetAccountInfo : AuthEventSink()

    data class ChangePassword(
        val oldPassword: String,
        val newPassword: String,
        val newPasswordConfirmation: String,
    ) : AuthEventSink()

    data object ResetState : AuthEventSink()

}


