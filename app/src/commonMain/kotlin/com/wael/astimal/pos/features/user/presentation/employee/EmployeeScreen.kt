package com.wael.astimal.pos.features.user.presentation.employee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    onBack: () -> Unit, viewModel: EmployeeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EmployeeScreen(
        state = state,
        onEvent = viewModel::processEvent,
        onBack = onBack
    )
}

@Composable
fun EmployeeScreen(
    state: EmployeeReducer.State,
    onEvent: (EmployeeReducer.Event) -> Unit,
    onBack: () -> Unit
) {
    val language = LocalAppLocale.current

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = state.isNewEmployee,
        lastModifiedDate = state.selectedEmployee?.updatedAt,
        onQueryChange = { onEvent(EmployeeReducer.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(EmployeeReducer.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(EmployeeReducer.Event.SearchActiveChanged(it)) },
        onBack = onBack,
        onDelete = { onEvent(EmployeeReducer.Event.DeleteClicked) },
        onCreate = { onEvent(EmployeeReducer.Event.SaveClicked) },
        onUpdate = { onEvent(EmployeeReducer.Event.SaveClicked) },
        onNew = { onEvent(EmployeeReducer.Event.NewEmployeeClicked) },
        enableFab = state.canSave,
        searchResults = {
            ItemGrid(
                list = state.filteredEmployees,
                onItemClick = { onEvent(EmployeeReducer.Event.EmployeeSelected(it)) },
                label = { Label(it.localizedName.displayName(language)) },
                isSelected = { it.id == state.selectedEmployee?.id },
            )
        }) {
        LabeledTextField(
            value = state.arName,
            onValueChange = { onEvent(EmployeeReducer.Event.ArNameChanged(it)) },
            label = stringResource(Res.string.ar_name),
            enabled = state.canEdit,
        )

        LabeledTextField(
            value = state.enName,
            onValueChange = { onEvent(EmployeeReducer.Event.EnNameChanged(it)) },
            label = stringResource(Res.string.en_name),
            enabled = state.canEdit,
        )

        LabeledTextField(
            value = state.email,
            onValueChange = { onEvent(EmployeeReducer.Event.EmailChanged(it)) },
            label = stringResource(Res.string.email),
            enabled = state.canEdit,
        )

        PasswordTextField(
            label = stringResource(Res.string.password),
            value = state.password,
            onValueChange = { onEvent(EmployeeReducer.Event.PasswordChanged(it)) },
            isVisible = state.isPasswordVisible,
            onVisibilityToggle = { onEvent(EmployeeReducer.Event.TogglePasswordVisibility) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            enabled = state.canEdit
        )

        PasswordTextField(
            label = stringResource(Res.string.confirm_password),
            value = state.confirmPassword,
            onValueChange = { onEvent(EmployeeReducer.Event.ConfirmPasswordChanged(it)) },
            isVisible = state.isConfirmPasswordVisible,
            onVisibilityToggle = { onEvent(EmployeeReducer.Event.ToggleConfirmPasswordVisibility) },
            onDone = { onEvent(EmployeeReducer.Event.SaveClicked) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            enabled = state.canEdit
        )

        Row(
            modifier = Modifier.padding(top = 32.dp).height(OutlinedTextFieldDefaults.MinHeight)
                .width(320.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.can_handle_private_partners),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = state.canHandlePrivatePartner,
                onCheckedChange = {
                    onEvent(
                        EmployeeReducer.Event.CanHandlePrivatePartnerChanged(
                            it
                        )
                    )
                },
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
