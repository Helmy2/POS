package com.wael.astimal.pos.features.management.presentation.business_partner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.displayName
import com.wael.astimal.pos.core.presentation.compoenents.ConfirmDeleteDialog
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.FAB
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.SearchBarWithBackButton
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.user.domain.entity.User
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.add_partner
import pos.app.generated.resources.address
import pos.app.generated.resources.address_with_args
import pos.app.generated.resources.amount
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.cancel
import pos.app.generated.resources.create
import pos.app.generated.resources.delete
import pos.app.generated.resources.edit
import pos.app.generated.resources.edit_partner
import pos.app.generated.resources.en_name
import pos.app.generated.resources.is_private
import pos.app.generated.resources.owns_partner
import pos.app.generated.resources.partner_owns
import pos.app.generated.resources.partner_type
import pos.app.generated.resources.phone
import pos.app.generated.resources.phone_placeholder
import pos.app.generated.resources.responsible_employee
import pos.app.generated.resources.type
import pos.app.generated.resources.update


@Composable
fun BusinessPartnerRoute(
    viewModel: BusinessPartnerViewModel = koinViewModel(),
    isOpenNew: Boolean,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(isOpenNew) {
        viewModel.processEvent(BusinessPartnerContract.Event.LoadInitialData(isOpenNew))
    }


    BusinessPartnerScreen(
        state = state,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun BusinessPartnerScreen(
    state: BusinessPartnerContract.State,
    onEvent: (BusinessPartnerContract.Event) -> Unit,
) {
    Screen(
        topBar = {
            SearchBarWithBackButton(
                query = state.searchQuery,
                onBack = { onEvent(BusinessPartnerContract.Event.BackClicked) },
                onQueryChange = { onEvent(BusinessPartnerContract.Event.SearchQueryChanged(it)) },
                onSearch = { onEvent(BusinessPartnerContract.Event.SearchQueryChanged(it)) },
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            FAB(
                enable = state.canCreate,
                onClick = { onEvent(BusinessPartnerContract.Event.AddNewPartnerClicked) }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_partner),
                )
            }
        },
    ) {
        BusinessPartnerList(
            partners = state.filteredPartners,
            onPartnerClick = { onEvent(BusinessPartnerContract.Event.PartnerClicked(it)) },
        )
    }

    when (val dialog = state.dialog) {
        is BusinessPartnerContract.Dialog.Details -> {
            Dialog(onDismissRequest = { onEvent(BusinessPartnerContract.Event.DismissDialog) }) {
                Card {
                    BusinessPartnerDetailView(
                        partner = dialog.partner,
                        canUpdate = state.canUpdate,
                        canDelete = state.canDelete,
                        onEvent = onEvent
                    )
                }
            }
        }

        is BusinessPartnerContract.Dialog.Edit -> {
            BusinessPartnerEditDialog(
                partner = dialog.partner,
                isSaving = state.isLoading,
                canEdit = state.canEdit,
                users = state.userDropdownData,
                onDismiss = { onEvent(BusinessPartnerContract.Event.DismissDialog) },
                onCreate = { partner, amount ->
                    onEvent(
                        BusinessPartnerContract.Event.CreateClicked(
                            partner, amount
                        )
                    )
                },
                onUpdate = { partner ->
                    onEvent(
                        BusinessPartnerContract.Event.UpdateClicked(
                            partner,
                        )
                    )
                },
            )
        }

        is BusinessPartnerContract.Dialog.None -> { /* Do nothing */
        }
    }
}


@Composable
fun BusinessPartnerDetailView(
    partner: BusinessPartner,
    canDelete: Boolean,
    canUpdate: Boolean,
    onEvent: (BusinessPartnerContract.Event) -> Unit
) {
    val language = LocalAppLocale.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    ConfirmDeleteDialog(
        show = showDeleteConfirmDialog,
        onConfirm = {
            showDeleteConfirmDialog = false
            onEvent(BusinessPartnerContract.Event.DeletePartnerClicked(partner))
        },
        onDismiss = { showDeleteConfirmDialog = false }
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Column {
            Text(partner.name.displayName(language), style = MaterialTheme.typography.headlineSmall)
            PartnerTypeChip(partnerType = partner.type)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(Res.string.address_with_args, partner.address))
        Text(stringResource(Res.string.phone_placeholder, partner.phone))

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showDeleteConfirmDialog = true }, enabled = canDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.delete),
                )
            }
            IconButton(onClick = {
                onEvent(
                    BusinessPartnerContract.Event.EditPartnerClicked(
                        partner
                    )
                )
            }, enabled = canUpdate) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.edit))
            }
        }
    }
}

@Composable
fun BusinessPartnerList(
    partners: List<BusinessPartner>,
    onPartnerClick: (BusinessPartner) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLocale.current
    LazyVerticalGrid(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        columns = GridCells.Adaptive(320.dp),
    ) {
        items(partners, key = { it.id }) { partner ->
            Card(modifier = Modifier.clickable { onPartnerClick(partner) }) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        partner.name.displayName(language),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    PartnerTypeChip(partnerType = partner.type)
                }
            }
        }
    }
}

@Composable
fun BusinessPartnerEditDialog(
    partner: BusinessPartner,
    users: List<User>,
    isSaving: Boolean,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onCreate: (BusinessPartner, amount: Double) -> Unit,
    onUpdate: (BusinessPartner) -> Unit
) {
    val language = LocalAppLocale.current
    var enName by remember(partner.name.enName) { mutableStateOf(partner.name.enName ?: "") }
    var arName by remember(partner.name.arName) { mutableStateOf(partner.name.arName ?: "") }
    var address by remember(partner.address) { mutableStateOf(partner.address) }
    var user by remember(partner.responsibleEmployee) { mutableStateOf<User?>(partner.responsibleEmployee) }
    var isReceiveMoney by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    val isNewPartner = partner.id == ""
    var type by remember { mutableStateOf<PartnerType?>(partner.type) }
    var isPrivate by remember { mutableStateOf(partner.isPrivate) }


    var phone1 by remember(partner.phone) {
        mutableStateOf(partner.phone.split(",").getOrNull(0) ?: "")
    }
    var phone2 by remember(partner.phone) {
        mutableStateOf(partner.phone.split(",").getOrNull(1) ?: "")
    }
    var phone3 by remember(partner.phone) {
        mutableStateOf(partner.phone.split(",").getOrNull(2) ?: "")
    }


    val imePadding = WindowInsets.ime.getBottom(LocalDensity.current).dp
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = imePadding)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isNewPartner) stringResource(Res.string.add_partner) else stringResource(
                        Res.string.edit_partner
                    ), style = MaterialTheme.typography.headlineSmall
                )

                // Form Fields
                LabeledTextField(
                    value = enName,
                    onValueChange = { enName = it },
                    label = stringResource(Res.string.en_name),
                    enabled = canEdit,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )
                LabeledTextField(
                    value = arName,
                    onValueChange = { arName = it },
                    label = stringResource(Res.string.ar_name),
                    enabled = canEdit,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                LabeledTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = stringResource(Res.string.address),
                    enabled = canEdit,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )
                LabeledTextField(
                    value = phone1,
                    onValueChange = { phone1 = it },
                    label = stringResource(Res.string.phone),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    enabled = canEdit
                )
                LabeledTextField(
                    value = phone2,
                    onValueChange = { phone2 = it },
                    label = stringResource(Res.string.phone),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    enabled = canEdit
                )
                LabeledTextField(
                    value = phone3,
                    onValueChange = { phone3 = it },
                    label = stringResource(Res.string.phone),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    enabled = canEdit
                )
            }

            ExposedDropdownMenu(
                label = stringResource(Res.string.responsible_employee),
                options = users.map { it.localizedName.displayName(language) },
                initialText = user?.localizedName.displayName(language),
                enabled = canEdit,
                onItemSelected = {
                    user = it?.let { users.getOrNull(it) }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            ExposedDropdownMenu(
                label = stringResource(Res.string.partner_type),
                options = PartnerType.entries.map { stringResource(it.getStringRes()) },
                initialText = type?.let { stringResource(it.getStringRes()) } ?: "",
                enabled = canEdit,
                onItemSelected = {
                    type = it?.let { PartnerType.entries.getOrNull(it) }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp).width(320.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.is_private), modifier = Modifier.weight(1f))
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    enabled = canEdit
                )
            }

            if (isNewPartner) {
                LabeledTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = stringResource(Res.string.amount),
                    enabled = true,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )

                ExposedDropdownMenu(
                    label = stringResource(Res.string.type),
                    options = listOf(
                        stringResource(Res.string.owns_partner),
                        stringResource(Res.string.partner_owns),
                    ),
                    initialText = "",
                    onItemSelected = {
                        isReceiveMoney = it == 0
                    },
                    enabled = true,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss, enabled = !isSaving
                ) { Text(stringResource(Res.string.cancel)) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (type != null && user != null) {
                            val updatedPartner = partner.copy(
                                name = partner.name.copy(enName = enName, arName = arName),
                                address = address,
                                phone = "$phone1,$phone2,$phone3",
                                type = type!!,
                                responsibleEmployee = user!!,
                                isPrivate = isPrivate
                            )
                            if (isNewPartner) {
                                onCreate(
                                    updatedPartner,
                                    if (isReceiveMoney) -(amount.toDoubleOrNull()
                                        ?: 0.0) else amount.toDoubleOrNull() ?: 0.0
                                )
                            } else {
                                onUpdate(updatedPartner)
                            }
                        }
                    },
                    enabled = !isSaving && (enName.isNotBlank() || arName.isNotBlank()) && type != null && user != null
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(if (isNewPartner) Res.string.create else Res.string.update))
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerTypeChip(partnerType: PartnerType) {
    SuggestionChip(
        onClick = {},
        label = {
            Text(
                stringResource(
                    partnerType.getStringRes()
                ), maxLines = 1
            )
        },
    )
}