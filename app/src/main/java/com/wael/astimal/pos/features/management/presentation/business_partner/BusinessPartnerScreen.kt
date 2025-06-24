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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.FAB
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.SearchBarWithBackButton
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

val PositiveBalanceColor = Color(0xFF43A047) // Green for when they owe you
val NegativeBalanceColor = Color(0xFFE53935) // Red for when you owe them

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
                    contentDescription = stringResource(R.string.add_partner),
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
                onSave = { partner, openingDebt, openingIndebtedness ->
                    onEvent(
                        BusinessPartnerContract.Event.SaveChangesClicked(
                            partner,
                            openingDebt,
                            openingIndebtedness
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
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = {
                Text(
                    stringResource(
                        R.string.confirm_delete_partner_message,
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
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Column {
            Text(partner.name.displayName(language), style = MaterialTheme.typography.headlineSmall)
            PartnerTypeChip(partnerType = partner.type)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.address_placeholder, partner.address))
        Text(stringResource(R.string.phone_placeholder, partner.phone))

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
                        contentDescription = stringResource(R.string.delete),
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
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
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
        items(partners, key = { it.getCompositeId() }) { partner ->
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
    onSave: (BusinessPartner, Double, Double) -> Unit
) {
    var enName by remember(partner.name.enName) { mutableStateOf(partner.name.enName ?: "") }
    var arName by remember(partner.name.arName) { mutableStateOf(partner.name.arName ?: "") }
    var address by remember(partner.address) { mutableStateOf(partner.address) }
    var phone by remember(partner.phone) { mutableStateOf(partner.phone) }
    var isClient by remember(partner.type) { mutableStateOf(partner.type == PartnerType.CLIENT || partner.type == PartnerType.BOTH) }
    var isSupplier by remember(partner.type) { mutableStateOf(partner.type == PartnerType.SUPPLIER || partner.type == PartnerType.BOTH) }

    var openingDebt by remember { mutableStateOf("0.0") }
    var openingIndebtedness by remember { mutableStateOf("0.0") }

    val isNewPartner = partner.clientId == null && partner.supplierId == null

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isNewPartner) stringResource(R.string.add_partner) else stringResource(
                        R.string.edit_partner
                    ),
                    style = MaterialTheme.typography.headlineSmall
                )

                // Form Fields
                LabeledTextField(
                    value = enName,
                    onValueChange = { enName = it },
                    label = stringResource(R.string.en_name),
                    enabled = canEdit
                )
                LabeledTextField(
                    value = arName,
                    onValueChange = { arName = it },
                    label = stringResource(R.string.ar_name),
                    enabled = canEdit
                )
                LabeledTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = stringResource(R.string.address),
                    enabled = canEdit
                )
                LabeledTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = stringResource(R.string.phone),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    enabled = canEdit
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isClient, onCheckedChange = { isClient = it })
                    Text(stringResource(R.string.is_client))
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = isSupplier, onCheckedChange = { isSupplier = it })
                    Text(stringResource(R.string.is_supplier))
                }

                AnimatedVisibility(visible = isNewPartner) {
                    Column {
                        if (isClient) {
                            LabeledTextField(
                                value = openingDebt,
                                onValueChange = { openingDebt = it },
                                label = stringResource(R.string.opening_debt),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                enabled = canEdit
                            )
                        }
                        if (isSupplier) {
                            LabeledTextField(
                                value = openingIndebtedness,
                                onValueChange = { openingIndebtedness = it },
                                label = stringResource(R.string.opening_indebtedness),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                enabled = canEdit
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) { Text(stringResource(R.string.cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val type = when {
                                isClient && isSupplier -> PartnerType.BOTH
                                isClient -> PartnerType.CLIENT
                                else -> PartnerType.SUPPLIER
                            }
                            val updatedPartner = partner.copy(
                                name = partner.name.copy(enName = enName, arName = arName),
                                address = address,
                                phone = phone,
                                type = type
                            )
                            onSave(
                                updatedPartner,
                                openingDebt.toDoubleOrNull() ?: 0.0,
                                openingIndebtedness.toDoubleOrNull() ?: 0.0
                            )
                        },
                        enabled = !isSaving && (isClient || isSupplier) && (enName.isNotBlank() || arName.isNotBlank())
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerTypeChip(partnerType: PartnerType) {
    val text = when (partnerType) {
        PartnerType.CLIENT -> stringResource(R.string.client)
        PartnerType.SUPPLIER -> stringResource(R.string.supplier)
        PartnerType.BOTH -> stringResource(R.string.client_and_supplier)
    }
    SuggestionChip(onClick = {}, label = { Text(text, maxLines = 1) })
}

/**
 * A reusable composable to display the net balance of a partner with appropriate
 * coloring and text based on whether they owe money or are owed money.
 */
@Composable
fun BalanceText(partner: BusinessPartner) {
    val balance = partner.netBalance
    val (balanceText, balanceColor) = when {
        // They owe you (positive balance)
        balance > 0.01 -> stringResource(
            R.string.net_balance_positive,
            "%.2f".format(balance)
        ) to PositiveBalanceColor
        // You owe them (negative balance)
        balance < -0.01 -> stringResource(
            R.string.net_balance_negative,
            "%.2f".format(abs(balance))
        ) to NegativeBalanceColor
        // Settled
        else -> stringResource(R.string.balance_summary_settled) to MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = balanceText,
        fontWeight = FontWeight.SemiBold,
        color = balanceColor,
        style = MaterialTheme.typography.bodyMedium
    )
}

fun BusinessPartner.getCompositeId(): String {
    return "${type}_${clientId?.local}_${supplierId?.local}"
}
