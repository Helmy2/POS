package com.wael.astimal.pos.core.domain.entity

import com.wael.astimal.pos.R


enum class ThemeMode {
    System,
    Light,
    Dark;

    fun resource(): Int {
        return when (this) {
            System -> R.string.system_default
            Light -> R.string.light_mode
            Dark -> R.string.dark_mode
        }
    }
}



