package com.wael.astimal.pos.features.management.presentation.business_partner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
            FloatingActionButton(onClick = { onEvent(BusinessPartnerInfoEvent.AddNewPartnerClicked) }) {
                Icon(
                    Icons.Default.Add, contentDescription = stringResource(R.string.add_partner)
                )
            }
        },
        loading = state.loading,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            BusinessPartnerList(
                partners = state.searchResults,
                onPartnerClick = { onEvent(BusinessPartnerInfoEvent.SelectBusinessPartner(it)) },
                selectedPartnerId = state.selectedBusinessPartner?.getCompositeId()
            )
        }
    }

    // Show Detail Dialog
    if (state.showDetailDialog && state.selectedBusinessPartner != null) {
        Dialog(onDismissRequest = { onEvent(BusinessPartnerInfoEvent.DismissDetailDialog) }) {
            Card {
                BusinessPartnerDetailView(
                    partner = state.selectedBusinessPartner,
                    isAdmin = state.isAdmin,
                    onEvent = onEvent
                )
            }
        }
    }

    // Show Edit/Add Dialog
    if (state.showEditDialog && state.partnerToEdit != null) {
        BusinessPartnerEditDialog(
            partner = state.partnerToEdit,
            isSaving = state.isSaving,
            onDismiss = { onEvent(BusinessPartnerInfoEvent.DismissEditDialog) },
            onSave = { onEvent(BusinessPartnerInfoEvent.SavePartnerClicked(it)) })
    }
}


@Composable
fun BusinessPartnerList(
    partners: List<BusinessPartner>,
    onPartnerClick: (BusinessPartner) -> Unit,
    selectedPartnerId: String?
) {
    val language = LocalAppLocale.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(partners, key = { it.getCompositeId() }) { partner ->
            ListItem(
                headlineContent = {
                Text(
                    partner.name.displayName(language), fontWeight = FontWeight.Bold
                )
            },
                supportingContent = {
                    Column {
                        Text(
                            stringResource(
                                R.string.address_placeholder,
                                partner.address ?: stringResource(R.string.n_a)
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
                                Text(stringResource(R.string.debt, partner.clientDebt.toString()))
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
                },
                trailingContent = { PartnerTypeChip(partnerType = partner.type) },
                modifier = Modifier
                    .clickable { onPartnerClick(partner) }
                    .background(
                        if (partner.getCompositeId() == selectedPartnerId) MaterialTheme.colorScheme.inversePrimary
                        else Color.Transparent
                    ))
        }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                partner.name.displayName(language), style = MaterialTheme.typography.headlineMedium
            )
            PartnerTypeChip(partnerType = partner.type)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(
                R.string.address_placeholder, partner.address ?: stringResource(R.string.n_a)
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
        partner.phones.forEach { phone -> Text("- $phone") }
        partner.responsibleEmployee?.let {
            Text(
                stringResource(
                    R.string.responsible_employee, it.localizedName.displayName(language)
                )
            )
        }

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
fun BusinessPartnerEditDialog(
    partner: BusinessPartner,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (BusinessPartner) -> Unit
) {
    var enName by remember { mutableStateOf(partner.name.enName ?: "") }
    var arName by remember { mutableStateOf(partner.name.arName ?: "") }
    var address by remember { mutableStateOf(partner.address ?: "") }
    var phone by remember { mutableStateOf(partner.phones.firstOrNull() ?: "") }
    var isClient by remember { mutableStateOf(partner.type == PartnerType.CLIENT || partner.type == PartnerType.BOTH) }
    var isSupplier by remember { mutableStateOf(partner.type == PartnerType.SUPPLIER || partner.type == PartnerType.BOTH) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (partner.clientLocalId == null && partner.supplierLocalId == null) stringResource(
                        R.string.add_partner
                    ) else stringResource(R.string.edit_partner),
                    style = MaterialTheme.typography.headlineSmall
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
                                isSupplier -> PartnerType.SUPPLIER
                                else -> PartnerType.CLIENT // Default or show error
                            }
                            val updatedPartner = partner.copy(
                                name = partner.name.copy(enName = enName, arName = arName),
                                address = address,
                                phones = listOf(phone),
                                type = type
                            )
                            onSave(updatedPartner)
                        },
                        enabled = !isSaving && (isClient || isSupplier) && enName.isNotBlank() && arName.isNotBlank()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp),
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
