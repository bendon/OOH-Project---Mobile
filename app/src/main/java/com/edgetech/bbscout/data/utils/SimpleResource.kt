package com.edgetech.bbscout.data.utils

sealed class SimpleResource<T>(var data: T? = null, val message: String? = null, val exceptionType: BBScoutException? = null, val errorCode: Int? = null) {
    //class Loading<T>(data: T? = null): SimpleResource<T>(data)
    class Success<T>(data: T?): SimpleResource<T>(data)
    class Error<T>(message: String, type: BBScoutException = BBScoutException(), code: Int? = null,  data: T? = null): SimpleResource<T>(data, message, type, code)
}

inline fun <T,> SimpleResource<T>.onSuccess(action:  (T?) -> Unit): SimpleResource<T> {
    return when(this) {
        is SimpleResource.Error -> this
        is SimpleResource.Success -> {
            action(data)
            this
        }
    }
}
inline fun <T>SimpleResource<T>.onError(action: (BBScoutException?) -> Unit): SimpleResource<T> {
    return when(this) {
        is SimpleResource.Error -> {
            action(this.exceptionType)
            this
        }
        is SimpleResource.Success -> this
    }
}