package com.wael.astimal.pos.features.user.presentation.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.snackbar.ObserveEffect
import com.wael.astimal.pos.features.user.presentation.components.LabeledRow
import com.wael.astimal.pos.features.user.presentation.components.LanguageSettingRow
import com.wael.astimal.pos.features.user.presentation.components.ThemeSettingsRow
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    navController: NavHostController,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEffect(viewModel.effect, viewModel.effect) {
        when (it) {
            SettingsEffect.NavigateToLogin -> {
                navController.navigate(Destination.Auth) {
                    popUpTo(0)
                }
            }
        }
    }

    SettingsScreen(state = state, onEvent = viewModel::handleEvent)
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    Box() {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            LabeledRow(
                label = stringResource(R.string.name), content = {
                    Text(
                        text = state.user?.localizedName?.displayName(state.language)
                            ?: stringResource(R.string.n_a)
                    )
                }, modifier = Modifier
                    .sizeIn(maxWidth = 400.dp)
                    .fillMaxWidth()
            )
            ThemeSettingsRow(
                showDialog = state.showThemeDialog,
                onShowDialog = { onEvent(SettingsEvent.UpdateThemeDialog(it)) },
                themeMode = state.themeMode,
                onThemeChange = { onEvent(SettingsEvent.UpdateThemeMode(it)) },
                modifier = Modifier
                    .sizeIn(maxWidth = 400.dp)
                    .fillMaxWidth()
            )
            LanguageSettingRow(
                showDialog = state.showLanguageDialog,
                onShowDialog = { onEvent(SettingsEvent.UpdateLanguageDialog(it)) },
                language = state.language,
                onLanguageChange = { onEvent(SettingsEvent.UpdateLanguage(it)) },
                modifier = Modifier
                    .sizeIn(maxWidth = 400.dp)
                    .fillMaxWidth()
            )
            Button(
                onClick = { onEvent(SettingsEvent.Logout) },
            ) {
                Text(text = stringResource(R.string.logout))
            }
        }
    }
}

