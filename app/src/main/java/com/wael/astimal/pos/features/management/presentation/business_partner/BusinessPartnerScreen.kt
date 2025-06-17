package com.wael.astimal.pos.features.management.presentation.business_partner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.SearchBarWithBackButton
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

val PositiveBalanceColor = Color(0xFFE53935)
val NegativeBalanceColor = Color(0xFF43A047)

@Composable
fun BusinessPartnerRoute(
    onBack: () -> Unit,
    viewModel: BusinessPartnerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BusinessPartnerScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        eventFlow = viewModel.eventFlow,
    )
}

@Composable
fun BusinessPartnerScreen(
    state: BusinessPartnerInfoState,
    onEvent: (BusinessPartnerInfoEvent) -> Unit,
    onBack: () -> Unit,
    eventFlow: SharedFlow<UiEvent>,
) {
    Screen(
        eventFlow = eventFlow,
        topBar = {
            SearchBarWithBackButton(
                query = state.query,
                onBack = onBack,
                onQueryChange = { onEvent(BusinessPartnerInfoEvent.UpdateQuery(it)) },
                onSearch = { onEvent(BusinessPartnerInfoEvent.UpdateQuery(it)) },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                state.canEdit
            ) {
                FloatingActionButton(
                    onClick = { onEvent(BusinessPartnerInfoEvent.AddNewPartnerClicked) },
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_partner),
                    )
                }
            }
        },
        loading = state.loading,
    ) {
        BusinessPartnerList(
            partners = state.searchResults,
            onPartnerClick = { onEvent(BusinessPartnerInfoEvent.SelectBusinessPartner(it)) },
            selectedPartnerId = state.selectedBusinessPartner?.getCompositeId(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    if (state.showDetailDialog && state.selectedBusinessPartner != null) {
        Dialog(onDismissRequest = { onEvent(BusinessPartnerInfoEvent.DismissDetailDialog) }) {
            Card {
                BusinessPartnerDetailView(
                    partner = state.selectedBusinessPartner,
                    isAdmin = state.canEdit,
                    onEvent = onEvent
                )
            }
        }
    }

    if (state.showEditDialog && state.partnerToEdit != null) {
        BusinessPartnerEditDialog(
            partner = state.partnerToEdit,
            isSaving = state.isSaving,
            onDismiss = { onEvent(BusinessPartnerInfoEvent.DismissEditDialog) },
            onSave = { partner, openingDebt, openingIndebtedness ->
                onEvent(
                    BusinessPartnerInfoEvent.SavePartnerClicked(
                        partner, openingDebt, openingIndebtedness
                    )
                )
            })
    }
}


@Composable
fun BusinessPartnerDetailView(
    partner: BusinessPartner, isAdmin: Boolean, onEvent: (BusinessPartnerInfoEvent) -> Unit
) {
    val language = LocalAppLocale.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        // ... (existing detail view content)
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                partner.name.displayName(language), style = MaterialTheme.typography.headlineMedium
            )
            PartnerTypeChip(partnerType = partner.type)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(
                R.string.address_placeholder, partner.address
            )
        )

        if (partner.type == PartnerType.CLIENT || partner.type == PartnerType.BOTH) {
            Text(stringResource(R.string.debt, partner.clientDebt.toString()))
        }
        if (partner.type == PartnerType.SUPPLIER || partner.type == PartnerType.BOTH) {
            Text(stringResource(R.string.indebtedness, partner.supplierIndebtedness.toString()))
        }
        if (partner.type == PartnerType.BOTH) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            val balanceText = if (partner.netBalance >= 0) {
                stringResource(R.string.net_balance_positive, partner.netBalance)
            } else {
                stringResource(R.string.net_balance_negative, abs(partner.netBalance))
            }
            Text(
                text = balanceText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (partner.netBalance >= 0) PositiveBalanceColor else NegativeBalanceColor
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        Text(stringResource(R.string.phones), fontWeight = FontWeight.Bold)
        partner.phone.forEach { phone -> Text("- $phone") }
        Text(
            stringResource(
                R.string.responsible_employee,
                partner.responsibleEmployee.localizedName.displayName(language)
            )
        )

        // Admin Action Buttons
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
                IconButton(onClick = { onEvent(BusinessPartnerInfoEvent.EditPartnerClicked(partner)) }) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = {
                Text(
                    stringResource(
                        R.string.confirm_delete_partner_message, partner.name.displayName(language)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(BusinessPartnerInfoEvent.DeletePartnerClicked(partner))
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            })
    }
}


@Composable
fun BusinessPartnerList(
    partners: List<BusinessPartner>,
    onPartnerClick: (BusinessPartner) -> Unit,
    selectedPartnerId: String?,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLocale.current
    LazyVerticalGrid(
        modifier = modifier, columns = GridCells.Adaptive(250.dp)
    ) {
        items(partners, key = { it.getCompositeId() }) { partner ->
            Card {
                ListItem(
                    headlineContent = {
                    FlowRow(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            partner.name.displayName(language), fontWeight = FontWeight.Bold
                        )
                        PartnerTypeChip(partnerType = partner.type)
                    }
                }, supportingContent = {
                    Column {
                        Text(
                            stringResource(
                                R.string.address_placeholder,
                                partner.address
                            )
                        )
                        when (partner.type) {
                            PartnerType.BOTH -> {
                                val balanceText = if (partner.netBalance >= 0) {
                                    stringResource(
                                        R.string.net_balance_positive, partner.netBalance
                                    )
                                } else {
                                    stringResource(
                                        R.string.net_balance_negative, abs(partner.netBalance)
                                    )
                                }
                                Text(
                                    text = balanceText,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (partner.netBalance >= 0) PositiveBalanceColor else NegativeBalanceColor
                                )
                            }

                            PartnerType.CLIENT -> {
                                Text(
                                    stringResource(
                                        R.string.debt, partner.clientDebt.toString()
                                    )
                                )
                            }

                            else -> {
                                Text(
                                    stringResource(
                                        R.string.indebtedness,
                                        partner.supplierIndebtedness.toString()
                                    )
                                )
                            }
                        }
                    }
                }, modifier = Modifier
                        .clickable { onPartnerClick(partner) }
                        .background(
                            if (partner.getCompositeId() == selectedPartnerId) MaterialTheme.colorScheme.inversePrimary
                            else Color.Transparent
                        ))
            }
        }
    }
}

@Composable
fun BusinessPartnerEditDialog(
    partner: BusinessPartner,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (BusinessPartner, Double, Double) -> Unit
) {
    var enName by remember { mutableStateOf(partner.name.enName ?: "") }
    var arName by remember { mutableStateOf(partner.name.arName ?: "") }
    var address by remember { mutableStateOf(partner.address) }
    var phone by remember { mutableStateOf(partner.phone) }
    var isClient by remember { mutableStateOf(partner.type == PartnerType.CLIENT || partner.type == PartnerType.BOTH) }
    var isSupplier by remember { mutableStateOf(partner.type == PartnerType.SUPPLIER || partner.type == PartnerType.BOTH) }

    // State for the new opening balance fields
    var openingDebt by remember { mutableStateOf("0.0") }
    var openingIndebtedness by remember { mutableStateOf("0.0") }

    val isNewPartner = partner.clientLocalId == null && partner.supplierLocalId == null

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
                    ), style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = enName,
                    onValueChange = { enName = it },
                    label = { Text("English Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = arName,
                    onValueChange = { arName = it },
                    label = { Text("Arabic Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Partner Type", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isClient, onCheckedChange = { isClient = it })
                    Text("Client")
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = isSupplier, onCheckedChange = { isSupplier = it })
                    Text("Supplier")
                }

                // Show opening balance fields ONLY for new partners
                AnimatedVisibility(visible = isNewPartner) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Opening Balances", style = MaterialTheme.typography.labelLarge)
                        // Show client debt field if the partner is a client
                        if (isClient) {
                            OutlinedTextField(
                                value = openingDebt,
                                onValueChange = {
                                    openingDebt =
                                        it.filter { c -> c.isDigit() || c == '.' || c == '-' }
                                },
                                label = { Text("Opening Debt (What they owe you)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // Show supplier indebtedness field if the partner is a supplier
                        if (isSupplier) {
                            OutlinedTextField(
                                value = openingIndebtedness,
                                onValueChange = {
                                    openingIndebtedness =
                                        it.filter { c -> c.isDigit() || c == '.' || c == '-' }
                                },
                                label = { Text("Opening Indebtedness (What you owe them)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.cancel))
                    }
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
                        enabled = !isSaving && (isClient || isSupplier) && enName.isNotBlank() && arName.isNotBlank()
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
    SuggestionChip(onClick = { /* No action */ }, label = { Text(text) })
}

fun BusinessPartner.getCompositeId(): String {
    return "${this.type}_${this.clientLocalId}_${this.supplierLocalId}"
}
