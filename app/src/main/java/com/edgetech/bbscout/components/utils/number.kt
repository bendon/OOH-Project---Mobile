package com.edgetech.bbscout.components.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.text.format
import kotlin.text.toDouble

fun Double?.roundToFourDecimalPlaces(): Double? {
    if (this == null) return null
    val decimalFormat = DecimalFormat("#.####", DecimalFormatSymbols(Locale.ENGLISH))
    return decimalFormat.format(this).toDouble()
}