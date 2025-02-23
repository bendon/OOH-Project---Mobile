package com.edgetech.bbscout.components.utils




fun String?.ifEmptySetNull(): String? {
    return if (this.isNullOrEmpty()){
        null
    } else
        this
}