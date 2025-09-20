package com.wael.astimal.pos.features.user.presentation.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.compoenents.BackButton
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.reports.presentation.reports.ReportsReducer
import com.wael.astimal.pos.features.user.presentation.components.LanguageSettingRow
import com.wael.astimal.pos.features.user.presentation.components.ThemeSettingsRow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.logout
import pos.app.generated.resources.reports
import pos.app.generated.resources.settings
import pos.app.generated.resources.unknown_user

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = LocalAppLocale.current

    LaunchedEffect(key1 = Unit) {
        viewModel.processEvent(SettingsContract.Event.LoadSettings)
    }

    Screen(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.settings)) }, navigationIcon = {
                BackButton(onBack)
            })
        }
    ) {
        SettingsScreen(
            userName = state.user?.localizedName?.displayName(language)
                ?: stringResource(Res.string.unknown_user),
            avatarUrl = state.user?.avatarUrl ?: "",
            themeMode = state.themeMode,
            showThemeDialog = state.showThemeDialog,
            language = state.language,
            showLanguageDialog = state.showLanguageDialog,
            onShowThemeDialog = {
                viewModel.processEvent(SettingsContract.Event.ThemeDialogVisibilityChanged(it))
            },
            onThemeChange = {
                viewModel.processEvent(SettingsContract.Event.ThemeChanged(it))
            },
            onShowLanguageDialog = {
                viewModel.processEvent(SettingsContract.Event.LanguageDialogVisibilityChanged(it))
            },
            onLanguageChange = {
                viewModel.processEvent(SettingsContract.Event.LanguageChanged(it))
            },
            onLogoutClicked = {
                viewModel.processEvent(SettingsContract.Event.LogoutClicked)
            }
        )
    }
}


@Composable
fun SettingsScreen(
    userName: String,
    avatarUrl: String,
    themeMode: ThemeMode,
    showThemeDialog: Boolean,
    onShowThemeDialog: (Boolean) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    language: Language,
    showLanguageDialog: Boolean,
    onShowLanguageDialog: (Boolean) -> Unit,
    onLanguageChange: (Language) -> Unit,
    onLogoutClicked: () -> Unit,
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
            imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                .build(),
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = userName,
            style = MaterialTheme.typography.titleLarge
        )

        ThemeSettingsRow(
            showDialog = showThemeDialog,
            onShowDialog = onShowThemeDialog,
            themeMode = themeMode,
            onThemeChange = onThemeChange,
            modifier = Modifier.fillMaxWidth()
        )
        LanguageSettingRow(
            showDialog = showLanguageDialog,
            onShowDialog = onShowLanguageDialog,
            language = language,
            onLanguageChange = onLanguageChange,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onLogoutClicked,
        ) {
            Text(text = stringResource(Res.string.logout))
        }
    }
}