package com.wael.astimal.pos.core.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

fun LocalDateTime.toMillis(): Long {
    val zoneId = ZoneId.systemDefault()
    return atZone(zoneId).toInstant().toEpochMilli()
}

fun Long.convertToString(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(this))
}

/**
 * Extension function to convert a Long representing epoch milliseconds into a LocalDateTime object.
 *
 * This function correctly handles the conversion by considering the user's current system time zone.
 *
 * @receiver The Long value representing the number of milliseconds since the epoch.
 * @return The corresponding LocalDateTime object in the system's default time zone.
 */
fun Long.toLocalDateTime(): LocalDateTime {
    val instant = Instant.ofEpochMilli(this)
    val zoneId = ZoneId.systemDefault()
    return LocalDateTime.ofInstant(instant, zoneId)
}
