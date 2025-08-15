package com.wael.astimal.pos.core.util

import java.util.Locale


fun Double?.formate(precision: Int = 2): String {
    if (this == null) return ""
    return String.format(Locale.US, "%.${precision}f", this)
}