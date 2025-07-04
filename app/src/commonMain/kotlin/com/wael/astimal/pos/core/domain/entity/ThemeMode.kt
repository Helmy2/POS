package com.wael.astimal.pos.core.domain.entity

import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.dark_mode
import pos.app.generated.resources.light_mode
import pos.app.generated.resources.system_default


enum class ThemeMode {
    System,
    Light,
    Dark;

    fun resource(): StringResource {
        return when (this) {
            System -> Res.string.system_default
            Light -> Res.string.light_mode
            Dark -> Res.string.dark_mode
        }
    }
}



