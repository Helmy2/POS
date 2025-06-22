package com.wael.astimal.pos.core.util


/**
 * Formats an integer sequence number into a string with leading zeros.
 * For example, 1 becomes "001", 12 becomes "012". This uses the idiomatic
 * Kotlin `padStart` function.
 *
 * @param length The desired minimum length of the string. Defaults to 3.
 * @return The formatted string with leading zeros.
 */
fun Int.formatSequence(length: Int = 3): String {
    return toString().padStart(length, '0')
}