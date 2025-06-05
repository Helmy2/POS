package com.wael.astimal.pos.core.domain.entity

import androidx.compose.ui.unit.LayoutDirection
import com.wael.astimal.pos.R

enum class Language(val code: String, val layoutDirection: LayoutDirection) {
    English("en", LayoutDirection.Ltr), Arabic("ar", LayoutDirection.Rtl);

    fun resource(): Int {
        return when (this) {
            English -> R.string.english
            Arabic -> R.string.arabic
        }
    }

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code == code } ?: English
        }
    }
}