package com.edgetech.bbscout.components.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.edgetech.bbscout.components.domain_util.AppEventSink
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.IoErrorException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.NetworkErrorException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.NotFoundException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.ServerErrorException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.UnAuthenticatedException
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.BadRequestException
import com.edgetech.bbscout.data.utils.BBScoutException


@Composable
fun ErrorShowDialog(
    showErrorMessage: Boolean,
    customError: Map<BBScoutException, String>,
    error: BBScoutException,
    event: AppEventSink? = null,
    handleBySender: (BBScoutException) -> Unit = {},
    positiveText: String? = "Retry",
    negativeText: String? = "Cancel",
    onDismiss: () -> Unit = {},
    onPositive: (AppEventSink?, BBScoutException) -> Unit = { _, _ -> },
) {

    var statusDialogState by rememberSaveable {
        mutableStateOf(showErrorMessage)
    }



//    if (error.httpStatusCode == 401) {
//        statusDialogState = false
//        handleBySender(error)
//        return
//    }

    if (error is UnAuthenticatedException) {
        statusDialogState = false
        handleBySender(error)
        return
    }

    val errorMessage: String = if (customError.any { it.key.javaClass == error.javaClass }) {
        customError.filter { it.key.javaClass == error.javaClass }.values.first()
    } else {
        onErrorGetMessage(error)
    }


    ShowErrorDialog(
        statusDialogState,
        onDismissRequest = {
            statusDialogState = false
            onDismiss()
        },
        onConfirmation = {
            onPositive(event, error)
        },
        posText = positiveText,
        negText = negativeText,
        title = "Error",
        message = errorMessage
    )


}



fun onErrorGetMessage(error: BBScoutException): String {
    when(error){
        is NetworkErrorException -> {
            return "We are unable to reach the server, please check your internet connection if the problem persists please try again later."
        }

        is UnAuthenticatedException -> {
            return "Please login again"
        }

        is IoErrorException -> {
            return "An error occurred while processing your request, please try again later."
        }

        is ServerErrorException -> {
            return "An error occurred on the server, please try again later."
        }

        is BadRequestException -> {
            return "An error occurred while processing your request, please try again later."
        }

        is NotFoundException -> {
            return "The requested resource was not found, please try again later."
        }

        else -> {
            return "An error occurred, please try again later."
        }
    }
}