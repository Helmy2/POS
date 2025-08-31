package com.wael.astimal.pos.features.user.presentation.employee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.user.domain.entity.PermissionDetails
import com.wael.astimal.pos.features.user.presentation.components.PasswordTextField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.can_handle_private_partners
import pos.app.generated.resources.confirm_password
import pos.app.generated.resources.create
import pos.app.generated.resources.delete
import pos.app.generated.resources.email
import pos.app.generated.resources.en_name
import pos.app.generated.resources.password
import pos.app.generated.resources.permissions
import pos.app.generated.resources.resource
import pos.app.generated.resources.update
import pos.app.generated.resources.view

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
            modifier = Modifier.padding(top = 16.dp).height(OutlinedTextFieldDefaults.MinHeight)
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

        PermissionsEditor(
            permissions = state.permissions,
            onPermissionChange = { resourceKey, details ->
                onEvent(EmployeeReducer.Event.PermissionsChanged(resourceKey, details))
            },
            enabled = state.canEdit
        )

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

@Composable
private fun PermissionsEditor(
    permissions: Map<String, PermissionDetails>,
    onPermissionChange: (String, PermissionDetails) -> Unit,
    enabled: Boolean
) {
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
    Text(
        text = stringResource(Res.string.permissions),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    // Header Row for the permissions table
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(Res.string.resource),
            modifier = Modifier.weight(2f),
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(Res.string.view),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(Res.string.create),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(Res.string.update),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(Res.string.delete),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider()

    EmployeeReducer.screens.forEach { resource ->
        val currentPermissions = permissions[resource.key] ?: PermissionDetails()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Resource Name
            Text(stringResource(resource.label), modifier = Modifier.weight(2f))

            // View Switch
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Switch(
                    checked = currentPermissions.canView,
                    onCheckedChange = {
                        onPermissionChange(
                            resource.key,
                            currentPermissions.copy(canView = it)
                        )
                    },
                    enabled = enabled
                )
            }
            // Create Switch
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Switch(
                    checked = currentPermissions.canCreate,
                    onCheckedChange = {
                        onPermissionChange(
                            resource.key,
                            currentPermissions.copy(canCreate = it, canView = true)
                        )
                    },
                    enabled = enabled
                )
            }
            // Update Switch
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Switch(
                    checked = currentPermissions.canUpdate,
                    onCheckedChange = {
                        onPermissionChange(
                            resource.key,
                            currentPermissions.copy(canUpdate = it, canView = true)
                        )
                    },
                    enabled = enabled
                )
            }
            // Delete Switch
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Switch(
                    checked = currentPermissions.canDelete,
                    onCheckedChange = {
                        onPermissionChange(
                            resource.key,
                            currentPermissions.copy(canDelete = it, canView = true)
                        )
                    },
                    enabled = enabled
                )
            }
        }
    }
}
