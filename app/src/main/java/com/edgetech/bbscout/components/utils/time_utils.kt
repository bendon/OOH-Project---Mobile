package com.edgetech.bbscout.components.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.util.TimeZone


val defaultZoneId: ZoneId =
    ZoneId.of(TimeZone.getDefault().toZoneId().id)


fun Long.toLocalDate(zoneId: String = "UTC") =
    Instant.ofEpochMilli(this).atZone(ZoneId.of(zoneId)).toLocalDate()

fun Long.toLocalDateTime(zoneId: String = "UTC") =
    Instant.ofEpochMilli(this).atZone(ZoneId.of(zoneId)).toLocalDateTime()

fun Long.toLocalTime() = LocalTime.ofNanoOfDay(this * 1000)
fun LocalDate.toLong(zoneId: String = "UTC") =
    this.atStartOfDay().atZone(ZoneId.of(zoneId)).toInstant().toEpochMilli()

fun LocalDateTime.toLong(zoneId: String = "UTC") =
    this.atZone(ZoneId.of(zoneId)).toInstant().toEpochMilli()

fun LocalDateTime.toUtc(): LocalDateTime = this.atZone(ZoneId.systemDefault()).withZoneSameInstant(
    ZoneId.of("UTC")).toLocalDateTime()


fun LocalTime.toUtc(): LocalTime = LocalDateTime.of(LocalDate.now(), this).toUtc().toLocalTime()

fun LocalTime.toLong() = this.getLong(ChronoField.MICRO_OF_DAY)
fun now(zoneId: String = "UTC") = LocalDateTime.now(ZoneId.of(zoneId))