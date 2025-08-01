package com.wael.astimal.pos.features.user.presentation.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
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
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.user.presentation.components.PasswordTextField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.can_handle_private_partners
import pos.app.generated.resources.confirm_password
import pos.app.generated.resources.email
import pos.app.generated.resources.en_name
import pos.app.generated.resources.password

@Composable
fun EmployeeRoute(
    onBack: () -> Unit,
    viewModel: EmployeeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredEmployees by viewModel.filteredEmployeesState.collectAsStateWithLifecycle()

    ObserveEffect(viewModel.effect) { effect ->
        when (effect) {
            is EmployeeContract.Effect.NavigateBack -> onBack()
        }
    }

    EmployeeScreen(
        state = state,
        filteredEmployees = filteredEmployees,
        onEvent = viewModel::processEvent,
        onBack = { viewModel.processEvent(EmployeeContract.Event.BackClicked) }
    )
}

@Composable
fun EmployeeScreen(
    state: EmployeeContract.State,
    filteredEmployees: List<com.wael.astimal.pos.features.user.domain.entity.User>,
    onEvent: (EmployeeContract.Event) -> Unit,
    onBack: () -> Unit
) {
    val language = LocalAppLocale.current

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = state.isNewEmployee,
        lastModifiedDate = state.selectedEmployee?.updatedAt,
        onQueryChange = { onEvent(EmployeeContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(EmployeeContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(EmployeeContract.Event.SearchActiveChanged(it)) },
        onBack = onBack,
        onDelete = { onEvent(EmployeeContract.Event.DeleteClicked) },
        onCreate = { onEvent(EmployeeContract.Event.SaveClicked) },
        onUpdate = { onEvent(EmployeeContract.Event.SaveClicked) },
        onNew = { onEvent(EmployeeContract.Event.NewEmployeeClicked) },
        canEdit = state.canEdit,
        searchResults = {
            ItemGrid(
                list = filteredEmployees,
                onItemClick = { onEvent(EmployeeContract.Event.EmployeeSelected(it)) },
                label = { Label(it.localizedName.displayName(language)) },
                isSelected = { it.id == state.selectedEmployee?.id },
            )
        }
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.can_handle_private_partners),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.canHandlePrivatePartner,
                    onCheckedChange = {
                        onEvent(
                            EmployeeContract.Event.CanHandlePrivatePartnerChanged(
                                it
                            )
                        )
                    },
                )
            }
        }

        item {
            LabeledTextField(
                value = state.enName,
                onValueChange = { onEvent(EmployeeContract.Event.EnNameChanged(it)) },
                label = stringResource(Res.string.en_name),
                enabled = state.canEdit,
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            LabeledTextField(
                value = state.arName,
                onValueChange = { onEvent(EmployeeContract.Event.ArNameChanged(it)) },
                label = stringResource(Res.string.ar_name),
                enabled = state.canEdit,
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            LabeledTextField(
                value = state.email,
                onValueChange = { onEvent(EmployeeContract.Event.EmailChanged(it)) },
                label = stringResource(Res.string.email),
                enabled = state.canEdit,
                modifier = Modifier.padding(8.dp)
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(stringResource(Res.string.password))
                PasswordTextField(
                    value = state.password,
                    onValueChange = { onEvent(EmployeeContract.Event.PasswordChanged(it)) },
                    isVisible = state.isPasswordVisible,
                    onVisibilityToggle = { onEvent(EmployeeContract.Event.TogglePasswordVisibility) },
                    onDone = {},
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canEdit
                )
            }
        }
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(stringResource(Res.string.confirm_password))
                PasswordTextField(
                    value = state.confirmPassword,
                    onValueChange = { onEvent(EmployeeContract.Event.ConfirmPasswordChanged(it)) },
                    isVisible = state.isConfirmPasswordVisible,
                    onVisibilityToggle = { onEvent(EmployeeContract.Event.ToggleConfirmPasswordVisibility) },
                    onDone = { onEvent(EmployeeContract.Event.SaveClicked) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canEdit
                )
            }
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
