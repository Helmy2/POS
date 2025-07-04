package com.wael.astimal.pos.core.domain.entity

import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.arabic
import pos.app.generated.resources.english

enum class Language(val code: String, val country: String, val layoutDirection: LayoutDirection) {
    English("en", "us", LayoutDirection.Ltr), Arabic("ar", "eg", LayoutDirection.Rtl);

    fun resource(): StringResource {
        return when (this) {
            English -> Res.string.english
            Arabic -> Res.string.arabic
        }
    }

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code == code } ?: English
        }
    }
}