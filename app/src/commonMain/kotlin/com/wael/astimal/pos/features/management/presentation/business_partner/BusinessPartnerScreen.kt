package com.wael.astimal.pos.features.management.presentation.business_partner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.FAB
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.SearchBarWithBackButton
import com.wael.astimal.pos.core.presentation.theme.CreditColor
import com.wael.astimal.pos.core.presentation.theme.DebitColor
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.add_partner
import pos.app.generated.resources.address
import pos.app.generated.resources.address_placeholder
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.balance_summary_settled
import pos.app.generated.resources.cancel
import pos.app.generated.resources.client
import pos.app.generated.resources.client_and_supplier
import pos.app.generated.resources.confirm_delete
import pos.app.generated.resources.confirm_delete_partner_message
import pos.app.generated.resources.delete
import pos.app.generated.resources.edit
import pos.app.generated.resources.edit_partner
import pos.app.generated.resources.en_name
import pos.app.generated.resources.opening_balance
import pos.app.generated.resources.opining_balance_negative_with_args
import pos.app.generated.resources.opining_balance_positive_with_args
import pos.app.generated.resources.partner_type
import pos.app.generated.resources.phone
import pos.app.generated.resources.phone_placeholder
import pos.app.generated.resources.save
import pos.app.generated.resources.supplier
import kotlin.math.abs


@Composable
fun BusinessPartnerRoute(
    viewModel: BusinessPartnerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredPartners by viewModel.filteredPartnersState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.processEvent(BusinessPartnerContract.Event.LoadInitialData)
    }

    BusinessPartnerScreen(
        state = state,
        filteredPartners = filteredPartners,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun BusinessPartnerScreen(
    state: BusinessPartnerContract.State,
    filteredPartners: List<BusinessPartner>,
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
                enable = state.canUserEdit,
                onClick = { onEvent(BusinessPartnerContract.Event.AddNewPartnerClicked) }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_partner),
                )
            }
        },
    ) {
        BusinessPartnerList(
            partners = filteredPartners,
            onPartnerClick = { onEvent(BusinessPartnerContract.Event.PartnerClicked(it)) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    when (val dialog = state.dialog) {
        is BusinessPartnerContract.Dialog.Details -> {
            Dialog(onDismissRequest = { onEvent(BusinessPartnerContract.Event.DismissDialog) }) {
                Card {
                    BusinessPartnerDetailView(
                        partner = dialog.partner,
                        isAdmin = state.canUserEdit,
                        onEvent = onEvent
                    )
                }
            }
        }

        is BusinessPartnerContract.Dialog.Edit -> {
            BusinessPartnerEditDialog(
                partner = dialog.partner,
                isSaving = state.isLoading,
                canEdit = state.canUserEdit,
                onDismiss = { onEvent(BusinessPartnerContract.Event.DismissDialog) },
                onSave = { partner ->
                    onEvent(
                        BusinessPartnerContract.Event.SaveChangesClicked(
                            partner,
                        )
                    )
                }
            )
        }

        is BusinessPartnerContract.Dialog.None -> { /* Do nothing */
        }
    }
}


@Composable
fun BusinessPartnerDetailView(
    partner: BusinessPartner, isAdmin: Boolean, onEvent: (BusinessPartnerContract.Event) -> Unit
) {
    val language = LocalAppLocale.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(Res.string.confirm_delete)) },
            text = {
                Text(
                    stringResource(
                        Res.string.confirm_delete_partner_message,
                        partner.name.displayName(language)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(BusinessPartnerContract.Event.DeletePartnerClicked(partner))
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Column {
            Text(partner.name.displayName(language), style = MaterialTheme.typography.headlineSmall)
            PartnerTypeChip(partnerType = partner.type)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(Res.string.address_placeholder, partner.address))
        Text(stringResource(Res.string.phone_placeholder, partner.phone))

        Spacer(modifier = Modifier.height(16.dp))

        // --- CHANGE: Unified Balance Display ---
        BalanceText(partner = partner)

        AnimatedVisibility(visible = isAdmin) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    onEvent(
                        BusinessPartnerContract.Event.EditPartnerClicked(
                            partner
                        )
                    )
                }) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.edit))
                }
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
    LazyVerticalGrid(modifier = modifier, columns = GridCells.Adaptive(250.dp)) {
        items(partners, key = { it.id.local }) { partner ->
            Card(modifier = Modifier.clickable { onPartnerClick(partner) }) {
                ListItem(
                    headlineContent = {
                        Column {
                            Text(
                                partner.name.displayName(language),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            PartnerTypeChip(partnerType = partner.type)
                        }
                    },
                    supportingContent = {
                        Column {
                            partner.address.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            BalanceText(partner = partner)
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun BusinessPartnerEditDialog(
    partner: BusinessPartner,
    isSaving: Boolean,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onSave: (BusinessPartner) -> Unit
) {
    var enName by remember(partner.name.enName) { mutableStateOf(partner.name.enName ?: "") }
    var arName by remember(partner.name.arName) { mutableStateOf(partner.name.arName ?: "") }
    var address by remember(partner.address) { mutableStateOf(partner.address) }
    var phone by remember(partner.phone) { mutableStateOf(partner.phone) }

    var openingBalance by remember { mutableStateOf("0.0") }
    val isNewPartner = partner.id == Id.new
    var type by remember {
        mutableStateOf(partner.type)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isNewPartner) stringResource(Res.string.add_partner) else stringResource(
                        Res.string.edit_partner
                    ),
                    style = MaterialTheme.typography.headlineSmall
                )

                // Form Fields
                LabeledTextField(
                    value = enName,
                    onValueChange = { enName = it },
                    label = stringResource(Res.string.en_name),
                    enabled = canEdit
                )
                LabeledTextField(
                    value = arName,
                    onValueChange = { arName = it },
                    label = stringResource(Res.string.ar_name),
                    enabled = canEdit
                )
                LabeledTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = stringResource(Res.string.opening_balance),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    enabled = canEdit
                )

                LabeledTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = stringResource(Res.string.address),
                    enabled = canEdit
                )
                LabeledTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = stringResource(Res.string.phone),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    enabled = canEdit
                )

                AnimatedVisibility(visible = isNewPartner) {
                    Column {
                        LabeledTextField(
                            value = openingBalance,
                            onValueChange = { openingBalance = it },
                            label = stringResource(Res.string.opening_balance),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = canEdit
                        )
                    }
                }
            }

            CustomExposedDropdownMenu(
                label = stringResource(Res.string.partner_type),
                items = PartnerType.entries,
                currentSelection = type.name,
                enabled = canEdit,
                itemToDisplayString = { it.name },
                onItemSelected = {
                    type = it
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving
                ) { Text(stringResource(Res.string.cancel)) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val updatedPartner = partner.copy(
                            name = partner.name.copy(enName = enName, arName = arName),
                            address = address,
                            phone = phone,
                            type = type,
                            openingBalance = openingBalance.toDoubleOrNull() ?: 0.0
                        )
                        onSave(
                            updatedPartner,
                        )
                    },
                    enabled = !isSaving && (enName.isNotBlank() || arName.isNotBlank())
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(Res.string.save))
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerTypeChip(partnerType: PartnerType) {
    val text = when (partnerType) {
        PartnerType.CLIENT -> stringResource(Res.string.client)
        PartnerType.SUPPLIER -> stringResource(Res.string.supplier)
        else -> stringResource(Res.string.client_and_supplier)
    }
    SuggestionChip(onClick = {}, label = { Text(text, maxLines = 1) })
}


@Composable
fun BalanceText(partner: BusinessPartner) {
    val balance = partner.openingBalance

    val (balanceText, balanceColor) = when {
        // They owe you (negative balance)
        balance > 0.01 -> stringResource(
            Res.string.opining_balance_negative_with_args,
            "%.2f".format(abs(balance))
        ) to DebitColor

        // You owe them (positive balance)
        balance < -0.01 -> stringResource(
            Res.string.opining_balance_positive_with_args,
            "%.2f".format(balance * -1)
        ) to CreditColor

        // Settled
        else -> stringResource(Res.string.balance_summary_settled) to MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = balanceText,
        fontWeight = FontWeight.SemiBold,
        color = balanceColor,
        style = MaterialTheme.typography.bodyMedium
    )
}