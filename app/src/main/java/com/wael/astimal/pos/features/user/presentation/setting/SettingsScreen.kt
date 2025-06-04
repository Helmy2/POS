package com.wael.astimal.pos.features.user.presentation.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.user.presentation.components.ClickableText
import com.wael.astimal.pos.features.user.presentation.components.LabeledRow
import com.wael.astimal.pos.features.user.presentation.components.LanguageSettingRow
import com.wael.astimal.pos.features.user.presentation.components.ThemeSettingsRow
import com.wael.astimal.pos.features.user.presentation.components.UpdateNameDialog
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    navController: NavHostController,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SettingsEffect.NavigateToLogin -> {
                    navController.navigate(Destination.Auth) {
                        popUpTo(0)
                    }
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
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            LabeledRow(
                label = stringResource(R.string.name),
                content = {
                    ClickableText(
                        content = {
                            Row {
                                Text(text = state.userSession.userName)
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Name"
                                )
                            }
                        },
                        onClick = { onEvent(SettingsEvent.UpdateEditeNameDialog(true)) }
                    )
                },
                modifier = Modifier.sizeIn(maxWidth = 400.dp).fillMaxWidth()
            )
            ThemeSettingsRow(
                showDialog = state.showThemeDialog,
                onShowDialog = { onEvent(SettingsEvent.UpdateThemeDialog(it)) },
                themeMode = state.themeMode,
                onThemeChange = { onEvent(SettingsEvent.UpdateThemeMode(it)) },
                modifier = Modifier.sizeIn(maxWidth = 400.dp).fillMaxWidth()
            )
            LanguageSettingRow(
                showDialog = state.showLanguageDialog,
                onShowDialog = { onEvent(SettingsEvent.UpdateLanguageDialog(it)) },
                language = state.language,
                onLanguageChange = { onEvent(SettingsEvent.UpdateLanguage(it)) },
                modifier = Modifier.sizeIn(maxWidth = 400.dp).fillMaxWidth()
            )
            Button(
                onClick = { onEvent(SettingsEvent.Logout) },
            ) {
                Text(text = stringResource(R.string.logout))
            }
        }
        AnimatedVisibility(state.showEditNameDialog) {
            UpdateNameDialog(
                name = state.name,
                onValueChange = { onEvent(SettingsEvent.UpdateName(it)) },
                onConfirm = { onEvent(SettingsEvent.ConfirmUpdateName) },
                onDismiss = { onEvent(SettingsEvent.UpdateEditeNameDialog(false)) },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

