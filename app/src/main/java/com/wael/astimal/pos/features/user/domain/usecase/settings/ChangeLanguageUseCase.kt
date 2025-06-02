package com.wael.astimal.pos.features.user.domain.usecase.settings

import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager

class ChangeLanguageUseCase(private val repo: SettingsManager) {
    suspend operator fun invoke(language: Language) {
        repo.changeLanguage(language)
    }
}