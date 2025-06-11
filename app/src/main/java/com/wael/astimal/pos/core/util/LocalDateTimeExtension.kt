package com.wael.astimal.pos.core.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
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
