package com.edgetech.bbscout.components.domain_util

import com.edgetech.bbscout.data.utils.BBScoutException


interface AppEventSink {
}

/**
 * use this if the only event happening are the error and empty event
 */
sealed class AppBasicUiEvent  {
    data class Error(val exception: BBScoutException, val eventSink : AppEventSink) : AppBasicUiEvent()

    data object Empty : AppBasicUiEvent()

    data class Loading(val isLoading: Boolean = false): AppBasicUiEvent()
}





