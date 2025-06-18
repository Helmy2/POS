package com.wael.astimal.pos.features.user.presentation.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.user.presentation.components.LanguageSettingRow
import com.wael.astimal.pos.features.user.presentation.components.ThemeSettingsRow
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.processEvent(SettingsContract.Event.LoadSettings)
    }

    SettingsScreen(
        state = state, onEvent = viewModel::processEvent
    )
}

@Composable
fun SettingsScreen(
    state: SettingsContract.State,
    onEvent: (SettingsContract.Event) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .sizeIn(maxWidth = 400.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            imageLoader = ImageLoader.Builder(LocalContext.current).crossfade(true)
                .components {
                    add(SvgDecoder.Factory())
                }.build(),
            model = state.user?.avatarUrl,
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        state.user?.let { user ->
            Text(
                text = user.localizedName.displayName(state.language),
                style = MaterialTheme.typography.titleLarge
            )
        }
        ThemeSettingsRow(
            showDialog = state.showThemeDialog,
            onShowDialog = { onEvent(SettingsContract.Event.ThemeDialogVisibilityChanged(it)) },
            themeMode = state.themeMode,
            onThemeChange = { onEvent(SettingsContract.Event.ThemeChanged(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        LanguageSettingRow(
            showDialog = state.showLanguageDialog,
            onShowDialog = { onEvent(SettingsContract.Event.LanguageDialogVisibilityChanged(it)) },
            language = state.language,
            onLanguageChange = { onEvent(SettingsContract.Event.LanguageChanged(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onEvent(SettingsContract.Event.LogoutClicked) },
        ) {
            Text(text = stringResource(R.string.logout))
        }
    }
}
