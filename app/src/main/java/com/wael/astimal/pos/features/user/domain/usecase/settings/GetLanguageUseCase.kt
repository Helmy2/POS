package com.wael.astimal.pos.features.user.domain.usecase.settings

import com.wael.astimal.pos.features.user.domain.repository.SettingsManager

class GetLanguageUseCase(private val repo: SettingsManager) {
    operator fun invoke() = repo.getLanguage()
}