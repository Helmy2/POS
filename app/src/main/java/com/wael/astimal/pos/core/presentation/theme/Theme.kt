package com.wael.astimal.pos.core.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.features.user.domain.usecase.settings.GetLanguageUseCase
import com.wael.astimal.pos.features.user.domain.usecase.settings.GetThemeModeUseCase
import org.koin.compose.koinInject
import java.util.Locale

private val DarkColorScheme = darkColorScheme(
    primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun POSTheme(
    dynamicColor: Boolean = true, content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val getThemeModeUseCase: GetThemeModeUseCase = koinInject()
    val getLanguageUseCase: GetLanguageUseCase = koinInject()
    val mode = getThemeModeUseCase().collectAsStateWithLifecycle(ThemeMode.System).value
    val language = getLanguageUseCase().collectAsStateWithLifecycle(Language.English).value

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
        LocalLayoutDirection provides language.layoutDirection,
        LocalAppLocale provides language.code,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}


object LocalAppLocale {
    private var default: Locale? = null
    val current: String
        @Composable get() = Locale.getDefault().toString()

    @Composable
    infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current

        if (default == null) {
            default = Locale.getDefault()
        }

        val new = when (value) {
            null -> default!!
            else -> Locale(value)
        }
        Locale.setDefault(new)
        configuration.setLocale(new)
        configuration.setLayoutDirection(new)
        val resources = LocalContext.current.resources

        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}