package com.wael.astimal.pos.core.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
object Clock {
    fun now(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }

    fun currentLocalDateTime(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }
}

@OptIn(ExperimentalTime::class)
fun String?.parseIsoTimestamp(): Long? {
    if (this.isNullOrBlank()) {
        return null
    }
    return try {
        Instant.parse(this).toEpochMilliseconds()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalTime::class)
fun Long.toDateString(): String {
    val customFormat = LocalDateTime.Format {
        day(); char('/'); monthNumber(); char('/'); year(); char(' ')
        hour(); char(':'); minute()
    }
    val instant = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return instant.format(customFormat)
}

fun Long.toISOString(): String {
    val instant = java.time.Instant.ofEpochMilli(this)
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}

@OptIn(ExperimentalTime::class)
fun LocalDateTime.format(): String {
    val customFormat = LocalDateTime.Format {
        day(); char('/'); monthNumber(); char('/'); year(); char(' ')
        hour(); char(':'); minute()
    }
    return this.format(customFormat)
}

@OptIn(ExperimentalTime::class)
fun LocalDateTime.toEpochMillis(): Long {
    return this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}