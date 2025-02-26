
package com.edgetech.bbscout.features.auth.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.edgetech.bbscout.R
import com.edgetech.bbscout.features.auth.domain.model.AuthEventSink
import com.edgetech.bbscout.features.auth.domain.model.AuthUiEvent
import com.edgetech.bbscout.features.auth.domain.model.AuthUiModel
import com.edgetech.bbscout.features.auth.domain.viewmodel.AuthViewmodel
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun LoadingScreen(
    authViewmodel: AuthViewmodel,
    navController: NavController
){
    LoadingScreenMain(
        authUiModel = authViewmodel.uiModel,
        navController = navController
    )
}

@Composable
fun LoadingScreenMain(
    authUiModel: AuthUiModel,
    navController: NavController
){

    val activity = LocalActivity.current
    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(true) {
        authUiModel.authEventSink(AuthEventSink.GetAccountInfo)
    }

    val uiEvent by authUiModel.authUiEvent.collectAsState()

    if (uiEvent is AuthUiEvent.LoginSuccessful){
        navController.navigate(AppDestinations.Dashboard)
    } else if (uiEvent is AuthUiEvent.Error){
        navController.navigate(AppDestinations.Login)
    }

    Scaffold {
        Column(modifier = Modifier.padding(it)) {
            Image(
                painter = painterResource(R.drawable.bbscout),
                null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        }
    }
}