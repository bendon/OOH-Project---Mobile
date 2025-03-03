package com.edgetech.bbscout.features.capture.domain.model

import com.edgetech.bbscout.data.utils.BBScoutException


data object BillboardTypeErrorException : BBScoutException()

data object LocationErrorException : BBScoutException()

data object BrandDescriptionErrorException : BBScoutException()

data object NoBillboardFoundException : BBScoutException()
