package com.wael.astimal.pos.features.dashboard.domain.entity

import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.monthly
import pos.app.generated.resources.today
import pos.app.generated.resources.weekly

enum class TimePeriod {
    TODAY, WEEKLY, MONTHLY;

    fun getStringRes(): StringResource {
        return when (this) {
            TODAY -> Res.string.today
            WEEKLY -> Res.string.weekly
            MONTHLY -> Res.string.monthly
        }
    }
}