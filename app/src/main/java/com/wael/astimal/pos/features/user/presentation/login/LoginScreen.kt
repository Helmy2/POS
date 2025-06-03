package com.wael.astimal.pos.features.user.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.user.presentation.components.AuthTextField
import com.wael.astimal.pos.features.user.presentation.components.CredentialsHeader
import com.wael.astimal.pos.features.user.presentation.components.PasswordTextField
import com.wael.astimal.pos.features.user.presentation.components.ProgressiveButton
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginRoute(
    navController: NavHostController,
    viewModel: LoginViewModel = koinViewModel(),
    snackbarState: SnackbarHostState,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LoginEffect.NavigateToHome -> {
                    navController.navigate(Destination.Main) {
                        popUpTo(0)
                    }
                }

                is LoginEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = context.getString(effect.message),
                    )
                }
            }
        }
    }

    LoginScreen(state = state, onEvent = viewModel::handleEvent)
}


@Composable
fun LoginScreen(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focus = LocalFocusManager.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        CredentialsHeader(
            title = R.string.welcome_back,
            body = R.string.login_to_your_account,
        )
        AuthTextField(
            value = state.username,
            label = stringResource(R.string.name),
            error = state.usernameError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            onValueChange = { onEvent(LoginEvent.UsernameChanged(it)) },
            modifier = Modifier
                .sizeIn(maxWidth = 600.dp)
                .fillMaxWidth()
        )

        PasswordTextField(
            value = state.password,
            error = state.passwordError,
            isVisible = state.isPasswordVisible,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            onVisibilityToggle = { onEvent(LoginEvent.TogglePasswordVisibility) },
            onDone = {
                focus.clearFocus()
                onEvent(LoginEvent.Login)
            },
            modifier = Modifier
                .sizeIn(maxWidth = 600.dp)
                .fillMaxWidth(),
            supportingText = if (state.passwordError != null) {
                {
                    Text(
                        text = stringResource(state.passwordError),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            } else {
                null
            },
        )

        ProgressiveButton(
            isLoading = state.loading,
            text = stringResource(R.string.login),
            onClick = {
                focus.clearFocus()
                onEvent(LoginEvent.Login)
            },
            modifier = Modifier
                .sizeIn(maxWidth = 600.dp)
                .fillMaxWidth(),
        )
    }
}

