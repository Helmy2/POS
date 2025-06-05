package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Language

data class LocalizedString(
    val arName: String?,
    val enName: String?,
) {
    fun displayName(
        language: Language
    ): String {
        return when (language) {
            Language.Arabic -> if (arName != null && arName.isNotBlank()) arName
            else if (enName != null && enName.isNotBlank()) enName else "N/A"
            Language.English -> if (enName != null && enName.isNotBlank()) enName
            else if (arName != null && arName.isNotBlank()) arName else "N/A"
        }
    }
}