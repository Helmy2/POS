package com.wael.astimal.pos.core.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


object Clock {
    fun now(): Long {
        return Instant.now().toEpochMilli()
    }

    fun getCurrentDateTime(): String {
        val instant = Instant.now()
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        return formatter.format(instant.atZone(ZoneId.systemDefault()))
    }
}

/**
 * Parses a timestamp string in ISO 8601 format (e.g., "2024-11-16T19:44:34.000000Z")
 * and converts it to a Long representing milliseconds since the Unix epoch.
 *
 * This approach uses the KMP-compatible kotlinx-datetime library, making it safe for
 * future migration.
 *
 * @receiver The nullable String to parse.
 * @return The time in milliseconds as a Long, or null if the string is null, blank, or invalid.
 */
fun String?.parseIsoTimestamp(): Long? {
    if (this.isNullOrBlank()) {
        return null
    }
    return try {
        Instant.parse(this).toEpochMilli()
    } catch (e: Exception) {
        // Log the exception or handle it as needed
        e.printStackTrace()
        null
    }
}

fun Long.convertToString(): String {
    val formatter = SimpleDateFormat("hh:mma ddMMM", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long.convertToDateString(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long.toLocalDateTime(): LocalDateTime {
    val instant = Instant.ofEpochMilli(this)
    val zoneId = ZoneId.systemDefault()
    return LocalDateTime.ofInstant(instant, zoneId)
}

fun Long.toDateString(): String {
    val instant = Instant.ofEpochMilli(this)
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}