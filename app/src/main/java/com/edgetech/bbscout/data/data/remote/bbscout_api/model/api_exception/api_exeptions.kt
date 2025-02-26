package com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception

import com.edgetech.bbscout.data.utils.BBScoutException


data class NetworkErrorException(override val message: String? = null): BBScoutException()

data class IoErrorException(override val message: String? = null): BBScoutException()

data class UnAuthenticatedException(override val message: String? = null): BBScoutException()

data class ServerErrorException(override val message: String? = null): BBScoutException()

data class BadRequestException(override val message: String? = null): BBScoutException()

data class NotFoundException(override val message: String? = null): BBScoutException()

