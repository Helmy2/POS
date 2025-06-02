package com.wael.astimal.pos.features.user.domain.usecase.settings

import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager
import kotlinx.coroutines.flow.Flow

class GetThemeModeUseCase(private val repo: SettingsManager) {
    operator fun invoke(): Flow<ThemeMode> {
        return repo.getThemeMode()
    }
}

