package com.wael.astimal.pos.core.domain.entity

data class LocalizedString(
    val arName: String? = null,
    val enName: String? = null,
) {

    fun contains(
        value: String
    ): Boolean {
        return arName?.lowercase()?.contains(value.lowercase(), ignoreCase = true) == true ||
                enName?.lowercase()?.contains(value.lowercase(), ignoreCase = true) == true
    }

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