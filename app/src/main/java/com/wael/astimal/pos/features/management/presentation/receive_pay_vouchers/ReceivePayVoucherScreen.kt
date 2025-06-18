package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.core.presentation.compoenents.BackButton
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.TextInputField
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceivePayVoucherRoute(
    onBack: () -> Unit,
    viewModel: ReceivePayVoucherViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReceivePayVoucherScreen(
        onBack = onBack,
        state = state,
        onEvent = viewModel::onEvent,
        eventFlow = viewModel.eventFlow
    )
}

@Composable
fun ReceivePayVoucherScreen(
    state: ReceivePayVoucherState,
    onEvent: (ReceivePayVoucherEvent) -> Unit,
    onBack: () -> Unit,
    eventFlow: SharedFlow<UiEvent>
) {
    val context = LocalContext.current

    if (state.showEditDialog && state.voucherToEdit != null) {
        EditVoucherDialog(
            voucher = state.voucherToEdit,
            isSaving = state.isSaving,
            onDismiss = { onEvent(ReceivePayVoucherEvent.DismissEditDialog) },
            onSave = { onEvent(ReceivePayVoucherEvent.SaveVoucher(it)) })
    }

    Screen(
        loading = state.isLoading, eventFlow = eventFlow, topBar = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
            ) {
                BackButton(onBack, Modifier.padding(16.dp))
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.client_or_suppler),
                    items = VoucherPartyType.entries,
                    selectedItemId = state.partyType.ordinal.toLong(),
                    onItemSelected = { it ->
                        it?.let { onEvent(ReceivePayVoucherEvent.SelectPartyType(it)) }
                    },
                    itemToDisplayString = { context.getString(it.getStringRes()) },
                    itemToId = { it.ordinal.toLong() },
                    canClearSelection = false,
                )
            }
        }) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AddVoucherForm(state = state, onEvent = onEvent, canEdit = state.canEdit)
            VoucherList(vouchers = state.vouchers, state = state, onEvent = onEvent)
        }
    }
}

@Composable
fun AddVoucherForm(
    state: ReceivePayVoucherState, onEvent: (ReceivePayVoucherEvent) -> Unit, canEdit: Boolean
) {
    val currentLanguage = LocalAppLocale.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(targetState = state.partyType) { partyType ->
            when (partyType) {
                VoucherPartyType.CLIENT -> CustomExposedDropdownMenu(
                    label = stringResource(R.string.client),
                    items = state.clients,
                    selectedItemId = state.selectedClient?.clientLocalId?.local,
                    onItemSelected = { onEvent(ReceivePayVoucherEvent.SelectClient(it)) },
                    itemToDisplayString = { it.name.displayName(currentLanguage) },
                    itemToId = { it.clientLocalId?.local },
                    canClearSelection = true,
                )

                VoucherPartyType.SUPPLIER -> CustomExposedDropdownMenu(
                    label = stringResource(R.string.supplier),
                    items = state.suppliers,
                    selectedItemId = state.selectedSupplier?.supplierLocalId?.local,
                    onItemSelected = { onEvent(ReceivePayVoucherEvent.SelectSupplier(it)) },
                    itemToDisplayString = { it.name.displayName(currentLanguage) },
                    itemToId = { it.supplierLocalId?.local },
                    canClearSelection = true
                )
            }
        }
        TextInputField(
            value = state.amount,
            onValueChange = { onEvent(ReceivePayVoucherEvent.UpdateAmount(it)) },
            label = stringResource(R.string.amount),
            enabled = canEdit
        )
        DataPicker(
            selectedDateMillis = state.date,
            onDateSelected = {
                onEvent(
                    ReceivePayVoucherEvent.UpdateDate(
                        it ?: System.currentTimeMillis()
                    )
                )
            },
            enabled = canEdit,
        )
        TextInputField(
            value = state.notes,
            onValueChange = { onEvent(ReceivePayVoucherEvent.UpdateNotes(it)) },
            label = stringResource(R.string.notes_optional),
            enabled = canEdit,
        )
        Button(
            onClick = { onEvent(ReceivePayVoucherEvent.AddVoucher) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !state.isSaving && canEdit
        ) {
            Text(stringResource(R.string.save_voucher))
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
    }
}


@Composable
fun VoucherList(
    vouchers: List<ReceivePayVoucher>,
    state: ReceivePayVoucherState,
    onEvent: (ReceivePayVoucherEvent) -> Unit
) {
    LazyColumn {
        items(vouchers, key = { it.id.local }) { voucher ->
            VoucherItem(
                voucher = voucher,
                canEdit = state.currentUser?.isAdmin == true || state.currentUser?.id == voucher.party.responsibleEmployee.id,
                onEdit = { onEvent(ReceivePayVoucherEvent.EditVoucherClicked(voucher)) },
                onDelete = { onEvent(ReceivePayVoucherEvent.DeleteVoucherClicked(voucher)) },
            )
        }
    }
}

@Composable
fun VoucherItem(
    voucher: ReceivePayVoucher, canEdit: Boolean, onEdit: () -> Unit, onDelete: () -> Unit
) {
    val date = remember(voucher.updatedAt) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            Date(voucher.updatedAt)
        )
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voucher.party.name.displayName(LocalAppLocale.current),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(voucher.partyType.getStringRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (voucher.notes.isNotBlank()) {
                    Text(text = voucher.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f".format(voucher.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = date, style = MaterialTheme.typography.bodySmall)
            }
            if (canEdit) {
                Row(modifier = Modifier.padding(start = 8.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true }, modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text("Are you sure you want to delete this voucher?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        stringResource(R.string.cancel)
                    )
                }
            })
    }
}


@Composable
fun EditVoucherDialog(
    voucher: ReceivePayVoucher,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (ReceivePayVoucher) -> Unit
) {
    var amount by remember { mutableStateOf(voucher.amount.toString()) }
    var notes by remember { mutableStateOf(voucher.notes) }
    var date by remember { mutableLongStateOf(voucher.createdAt) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    stringResource(R.string.edit_voucher),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                TextInputField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = stringResource(R.string.amount)
                )
                DataPicker(
                    selectedDateMillis = date,
                    onDateSelected = { date = it ?: System.currentTimeMillis() })
                TextInputField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = stringResource(R.string.notes_optional)
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = onDismiss, enabled = !isSaving
                    ) { Text(stringResource(R.string.cancel)) }
                    Button(
                        onClick = {
                            val newAmount = amount.toDoubleOrNull()
                            if (newAmount != null) {
                                onSave(
                                    voucher.copy(
                                        amount = newAmount,
                                        notes = notes,
                                        createdAt = date
                                    )
                                )
                            }
                        }, enabled = !isSaving
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
