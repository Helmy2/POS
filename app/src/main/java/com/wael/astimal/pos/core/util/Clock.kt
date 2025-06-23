package com.wael.astimal.pos.core.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


object Clock {
    @OptIn(ExperimentalTime::class)
    fun now(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}

fun Long.convertToString(): String {
    val formatter = SimpleDateFormat("h:ma ddMMM", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long.toLocalDateTime(): LocalDateTime {
    val instant = Instant.ofEpochMilli(this)
    val zoneId = ZoneId.systemDefault()
    return LocalDateTime.ofInstant(instant, zoneId)
}