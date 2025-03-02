package com.edgetech.bbscout.features.auth.domain.model

import com.edgetech.bbscout.data.utils.BBScoutException


data object PasswordDoNotMatchException : BBScoutException("Passwords do not match")

data object EmptyCredentialsException : BBScoutException("Email or password cannot be empty")

data object EmptyNameException : BBScoutException("Name cannot be empty")

data object RegistrationBadRequest : BBScoutException("Registration failed")

data object RequestPasswordResetBadRequest : BBScoutException("Password reset request failed")