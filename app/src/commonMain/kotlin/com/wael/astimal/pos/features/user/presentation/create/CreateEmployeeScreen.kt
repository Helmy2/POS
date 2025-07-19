package com.wael.astimal.pos.features.user.presentation.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.base.ObserveEffect
import com.wael.astimal.pos.core.presentation.compoenents.BackButton
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.features.user.presentation.components.PasswordTextField
import com.wael.astimal.pos.features.user.presentation.components.ProgressiveButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.confirm_password
import pos.app.generated.resources.create_employee
import pos.app.generated.resources.email
import pos.app.generated.resources.en_name
import pos.app.generated.resources.password

@Composable
fun CreateEmployeeRoute(
    onBack: () -> Unit,
    viewModel: CreateEmployeeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEffect(viewModel.effect) { effect ->
        when (effect) {
            is CreateEmployeeContract.Effect.NavigateBack -> onBack()
        }
    }

    CreateEmployeeScreen(
        state = state,
        onEvent = viewModel::processEvent,
        onBack = onBack
    )
}

@Composable
fun CreateEmployeeScreen(
    state: CreateEmployeeContract.State,
    onEvent: (CreateEmployeeContract.Event) -> Unit,
    onBack: () -> Unit
) {
    Screen(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .statusBarsPadding()
            ) {
                BackButton(onClick = onBack)
                Text(
                    text = stringResource(Res.string.create_employee),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LabeledTextField(
                value = state.enName,
                onValueChange = { onEvent(CreateEmployeeContract.Event.EnNameChanged(it)) },
                label = stringResource(Res.string.en_name),
                enabled = state.canEdit
            )
            LabeledTextField(
                value = state.arName,
                onValueChange = { onEvent(CreateEmployeeContract.Event.ArNameChanged(it)) },
                label = stringResource(Res.string.ar_name),
                enabled = state.canEdit
            )
            LabeledTextField(
                value = state.email,
                onValueChange = { onEvent(CreateEmployeeContract.Event.EmailChanged(it)) },
                label = stringResource(Res.string.email),
                enabled = state.canEdit
            )

            Text(stringResource(Res.string.password))
            PasswordTextField(
                value = state.password,
                onValueChange = { onEvent(CreateEmployeeContract.Event.PasswordChanged(it)) },
                isVisible = state.isPasswordVisible,
                onVisibilityToggle = { onEvent(CreateEmployeeContract.Event.TogglePasswordVisibility) },
                onDone = {},
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canEdit
            )
            Text(stringResource(Res.string.confirm_password))
            PasswordTextField(
                value = state.confirmPassword,
                onValueChange = { onEvent(CreateEmployeeContract.Event.ConfirmPasswordChanged(it)) },
                isVisible = state.isConfirmPasswordVisible,
                onVisibilityToggle = { onEvent(CreateEmployeeContract.Event.ToggleConfirmPasswordVisibility) },
                onDone = {},
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canEdit
            )
            Spacer(modifier = Modifier.weight(1f))
            ProgressiveButton(
                text = stringResource(Res.string.create_employee),
                onClick = { onEvent(CreateEmployeeContract.Event.CreateClicked) },
                isLoading = state.isLoading,
                enabled = state.canEdit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
