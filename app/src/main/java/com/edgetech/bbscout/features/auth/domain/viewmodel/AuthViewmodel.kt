package com.edgetech.bbscout.features.auth.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgetech.bbscout.components.di.IoDispatcher
import com.edgetech.bbscout.components.di.MainDispatcher
import com.edgetech.bbscout.components.file_saver.FileSaver
import com.edgetech.bbscout.components.location.GetLocationInfo
import com.edgetech.bbscout.components.utils.toJson
import com.edgetech.bbscout.components.utils.toLong
import com.edgetech.bbscout.data.data.local.enities.AuthEntity
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AccountResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AuthResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ChangePasswordRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.LoginRequest
import com.edgetech.bbscout.data.data.remote.gen_ai.llm.FulltextAndImageInference
import com.edgetech.bbscout.data.repositories.MainRepository
import com.edgetech.bbscout.data.utils.BBScoutException
import com.edgetech.bbscout.data.utils.onError
import com.edgetech.bbscout.data.utils.onSuccess
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.auth.domain.model.AuthUiEvent
import com.edgetech.bbscout.features.auth.domain.model.AuthUiModel
import com.edgetech.bbscout.features.auth.domain.model.AuthUiState
import com.edgetech.bbscout.features.auth.domain.model.EmptyCredentialsException
import com.edgetech.bbscout.features.auth.domain.model.PasswordDoNotMatchException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AuthViewmodel @Inject constructor(
    private val repository: MainRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {


    private val _uiState = MutableStateFlow(AuthUiState())

    private val _uiEvent = MutableStateFlow<AuthUiEvent>(AuthUiEvent.Empty)

    val uiModel = AuthUiModel(
        authUiEvent = _uiEvent,
        authUiState = _uiState
    ) { eventSink ->
        when (eventSink) {
            is AuthEventSink.ChangePassword -> {
                changePassword(eventSink)
            }

            is AuthEventSink.LoginWithEmailAndPassword -> {
                loginWithEmailAndPassword(eventSink)
            }

            is AuthEventSink.LoginWithGoogle -> {
                loginWithGoogle(eventSink)
            }

            is AuthEventSink.Logout -> {
                logout(eventSink)
            }

            is AuthEventSink.ResetState -> {
                resetState(eventSink)
            }

            is AuthEventSink.GetAccountInfo -> {
                getAccountInfo(eventSink)
            }
        }

    }

    private fun changePassword(eventSink: AuthEventSink.ChangePassword) {
        if (eventSink.newPassword != eventSink.newPasswordConfirmation) {
            _uiEvent.update {
                AuthUiEvent.Error(PasswordDoNotMatchException, eventSink)
            }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            repository.changePassword(
                ChangePasswordRequest(
                    oldPassword = eventSink.oldPassword,
                    newPassword = eventSink.newPassword
                )
            ).onSuccess {
                _uiEvent.update {
                    AuthUiEvent.PasswordChangeSuccessful
                }
            }.onError { ex ->
                _uiEvent.update {
                    AuthUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }
        }
    }

    private fun loginWithEmailAndPassword(eventSink: AuthEventSink.LoginWithEmailAndPassword) {
        if (eventSink.email.isEmpty() || eventSink.password.isEmpty()) {
            _uiEvent.update {
                AuthUiEvent.Error(EmptyCredentialsException, eventSink)
            }
            return
        }
        var loginIsSuccessful = false
        var authResponse: AuthResponse? = null
        viewModelScope.launch(ioDispatcher) {
            repository.login(
                LoginRequest(
                    email = eventSink.email,
                    password = eventSink.password
                )
            ).onSuccess {
                authResponse = it
                loginIsSuccessful = true
            }.onError { ex ->
                _uiEvent.update {
                    AuthUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }
            if (loginIsSuccessful) {
                login(authResponse, eventSink)
            }

        }
    }

    private fun loginWithGoogle(eventSink: AuthEventSink.LoginWithGoogle) {
        viewModelScope.launch(ioDispatcher) {
            var loginIsSuccessful = false
            var authResponse: AuthResponse? = null
            repository.loginWithGoogle(
                LoginRequest(
                    token = eventSink.token
                )
            ).onSuccess {
                authResponse = it
                loginIsSuccessful = true
            }.onError { ex ->
                _uiEvent.update {
                    AuthUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }
            if (loginIsSuccessful) {
                login(authResponse, eventSink)
            }
        }
    }

    private suspend fun login(pAuthResponse: AuthResponse?, eventSink: AuthEventSink) {
        var accountId = ""
        var authResponse = pAuthResponse
        val authEntity = AuthEntity(
            refreshToken = authResponse?.refreshToken,
            accessToken = authResponse?.accessToken,
            authObject = authResponse?.toJson(),
            timeAdded = LocalDateTime.now().toLong()
        )
        repository.addAuth(authEntity)
        repository.getAccounts().onSuccess {
            accountId = it?.firstOrNull()?.id ?: ""
        }.onError { ex ->
            _uiEvent.update {
                AuthUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
            }
        }

        if (accountId.isNotEmpty()) {
            repository.switchAccount(
                AccountResponse(accountId = accountId)
            ).onSuccess { auth ->
                authResponse = auth
                _uiEvent.update {
                    AuthUiEvent.LoginSuccessful(auth?.accessToken)
                }
            }.onError { ex ->
                _uiEvent.update {
                    AuthUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }

            if (authResponse != null) {
                val authEntity2 = AuthEntity(
                    refreshToken = authResponse!!.refreshToken,
                    accessToken = authResponse?.accessToken,
                    authObject = authResponse?.toJson(),
                    timeAdded = LocalDateTime.now().toLong()
                )
                repository.addAuth(authEntity2)
            }

        }
    }

    private fun logout(eventSink: AuthEventSink.Logout) {
        viewModelScope.launch(ioDispatcher) {
            repository.deleteAuth()
            _uiEvent.update {
                AuthUiEvent.LogoutSuccessful
            }
        }
    }

    private fun resetState(eventSink: AuthEventSink.ResetState) {
        _uiEvent.update {
            AuthUiEvent.Empty
        }
    }

    private fun getAccountInfo(eventSink: AuthEventSink.GetAccountInfo) {
        viewModelScope.launch(ioDispatcher){
            repository.getProfile().onSuccess {
                _uiEvent.update {
                    AuthUiEvent.LoginSuccessful("")
                }
            }.onError { ex ->
                _uiEvent.update {
                    AuthUiEvent.Error(ex ?: BBScoutException(), eventSink)
                }
            }

        }
    }


}