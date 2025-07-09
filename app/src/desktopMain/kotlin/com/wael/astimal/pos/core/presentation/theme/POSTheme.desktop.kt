package com.wael.astimal.pos.core.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import java.util.Locale

@Composable
actual fun getColorScheme(
    darkTheme: Boolean
): ColorScheme {
    return if (darkTheme) DarkColorScheme else LightColorScheme
}

@Composable
actual fun SystemAppearance(isDark: Boolean) {
}

@Composable
actual fun SetLocale() {
    val settingsManager: SettingsManager = koinInject()

    LaunchedEffect(Unit) {
        settingsManager.getLanguage().collectLatest {
            Locale.setDefault(Locale.forLanguageTag(it.code))
        }
    }
}