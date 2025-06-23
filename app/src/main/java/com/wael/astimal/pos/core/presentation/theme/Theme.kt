package com.wael.astimal.pos.core.presentation.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager
import org.koin.compose.koinInject
import java.util.Locale

val LightColorScheme: ColorScheme = lightColorScheme()

val DarkColorScheme: ColorScheme = darkColorScheme()

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun rememberLocalizedContext(language: Language): Context {
    val context = LocalContext.current
    val localeConfiguration = LocalConfiguration.current
    return remember(context, language) {
        val locale = Locale(language.code)
        Locale.setDefault(locale) // Set default locale for the JVM

        val configuration = Configuration(localeConfiguration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        context.createConfigurationContext(configuration)
    }
}

@Composable
fun POSTheme(
    dynamicColor: Boolean = true, content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val getThemeModeUseCase: SettingsManager = koinInject()
    val mode =
        getThemeModeUseCase.getThemeMode().collectAsStateWithLifecycle(ThemeMode.System).value
    val language =
        getThemeModeUseCase.getLanguage().collectAsStateWithLifecycle(Language.English).value

    val darkTheme = remember(mode) {
        when (mode) {
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
            ThemeMode.System -> isDark
        }
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalContext provides rememberLocalizedContext(language),
        LocalLayoutDirection provides language.layoutDirection,
        LocalAppLocale provides language,
    ) {
        key(language) {
            MaterialTheme(
                colorScheme = colorScheme,
                shapes = Shapes,
                content = content,
            )
        }
    }
}


val LocalAppLocale = compositionLocalOf { Language.English }