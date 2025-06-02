package com.wael.astimal.pos.features.user.domain.usecase.settings

import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager

class ChangeThemeModeUseCase(private val repo: SettingsManager) {
    suspend operator fun invoke(mode: ThemeMode) {
        repo.changeTheme(mode)
    }
}

