package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.ConfirmDeleteDialog
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.FAB
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.SearchBarWithBackButton
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.add_voucher
import pos.app.generated.resources.amount
import pos.app.generated.resources.business_partner
import pos.app.generated.resources.cancel
import pos.app.generated.resources.edit_voucher
import pos.app.generated.resources.new_voucher
import pos.app.generated.resources.notes_optional
import pos.app.generated.resources.owns_partner
import pos.app.generated.resources.partner_owns
import pos.app.generated.resources.pay_money
import pos.app.generated.resources.receive_money
import pos.app.generated.resources.save
import pos.app.generated.resources.transaction_type
import pos.app.generated.resources.type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun ReceivePayVoucherRoute(
    viewModel: ReceivePayVoucherViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredVouchers by viewModel.filteredVouchersState.collectAsStateWithLifecycle()

    ReceivePayVoucherScreen(
        onBack = { viewModel.processEvent(ReceivePayVoucherContract.Event.BackClicked) },
        state = state,
        filteredVouchers = filteredVouchers,
        onEvent = viewModel::processEvent
    )
}

@Composable
fun ReceivePayVoucherScreen(
    state: ReceivePayVoucherContract.State,
    onEvent: (ReceivePayVoucherContract.Event) -> Unit,
    onBack: () -> Unit,
    filteredVouchers: List<ReceivePayVoucher>
) {
    if (state.dialogState.show) {
        VoucherEditDialog(state = state, onEvent = onEvent)
    }

    Screen(
        topBar = {
            SearchBarWithBackButton(
                query = state.searchQuery,
                onBack = onBack,
                onQueryChange = { onEvent(ReceivePayVoucherContract.Event.SearchQueryChanged(it)) },
                onSearch = { onEvent(ReceivePayVoucherContract.Event.SearchQueryChanged(it)) },
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            FAB(
                onClick = {
                    if (state.canUserEdit) onEvent(ReceivePayVoucherContract.Event.AddVoucherClicked)
                }, enable = state.canUserEdit
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_voucher),
                )
            }
        },
    ) {
        LazyVerticalGrid(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Adaptive(320.dp),
        ) {
            items(filteredVouchers, key = { it.id }) { voucher ->
                VoucherItem(
                    voucher = voucher,
                    canEdit = state.canUserEdit && (voucher.transactionType == TransactionType.OPENING_BALANCE || voucher.transactionType == TransactionType.PAYMENT),
                    onEdit = { onEvent(ReceivePayVoucherContract.Event.EditVoucherClicked(voucher)) },
                    onDelete = {
                        onEvent(
                            ReceivePayVoucherContract.Event.DeleteVoucherClicked(
                                voucher
                            )
                        )
                    })
            }
        }
    }
}

@Composable
fun VoucherEditDialog(
    state: ReceivePayVoucherContract.State, onEvent: (ReceivePayVoucherContract.Event) -> Unit
) {
    val dialogState = state.dialogState
    val language = LocalAppLocale.current

    AlertDialog(
        onDismissRequest = { onEvent(ReceivePayVoucherContract.Event.DismissDialog) },
        title = {
            val titleRes =
                if (dialogState.voucherToEdit == null) Res.string.new_voucher else Res.string.edit_voucher
            Text(stringResource(titleRes))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.transaction_type),
                    items = TransactionType.getTypesForDropdown(),
                    selectedItemId = dialogState.transactionType.ordinal.toString(),
                    onItemSelected = {
                        onEvent(
                            ReceivePayVoucherContract.Event.DialogTransactionTypeSelected(
                                it
                            )
                        )
                    },
                    itemToDisplayString = { stringResource(it.getStringRes()) },
                    itemToId = { it.ordinal.toString() },
                    enabled = state.canUserEdit
                )


                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.business_partner),
                    items = state.partyDropdownData,
                    selectedItemId = dialogState.selectedPartnerId,
                    onItemSelected = {
                        onEvent(
                            ReceivePayVoucherContract.Event.DialogPartnerSelected(
                                it.id
                            )
                        )
                    },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id },
                    onClearItem = {
                        onEvent(
                            ReceivePayVoucherContract.Event.DialogPartnerSelected(
                                null
                            )
                        )
                    },
                    enabled = state.canUserEdit && dialogState.voucherToEdit == null
                )

                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.type),
                    items = listOf(true, false),
                    selectedItemId = dialogState.isReceiveMoney.toString(),
                    onItemSelected = {
                        onEvent(
                            ReceivePayVoucherContract.Event.DialogIsReceiveMoneyChanged(
                                it
                            )
                        )
                    },
                    itemToDisplayString = {
                        if (dialogState.transactionType == TransactionType.OPENING_BALANCE) {
                            if (it) stringResource(Res.string.partner_owns) else stringResource(Res.string.owns_partner)
                        } else {
                            if (it) stringResource(Res.string.receive_money) else stringResource(Res.string.pay_money)
                        }
                    },
                    itemToId = { it.toString() },
                    enabled = state.canUserEdit
                )

                LabeledTextField(
                    value = dialogState.amount, onValueChange = {
                        onEvent(
                            ReceivePayVoucherContract.Event.DialogAmountChanged(
                                it
                            )
                        )
                    }, label = stringResource(Res.string.amount), enabled = state.canUserEdit
                )

                DataPicker(
                    selectedDateMillis = dialogState.date, onDateSelected = {
                        onEvent(
                            ReceivePayVoucherContract.Event.DialogDateChanged(
                                it
                            )
                        )
                    }, enabled = state.canUserEdit
                )

                LabeledTextField(
                    value = dialogState.notes,
                    onValueChange = {
                        onEvent(
                            ReceivePayVoucherContract.Event.DialogNotesChanged(
                                it
                            )
                        )
                    },
                    label = stringResource(Res.string.notes_optional),
                    enabled = state.canUserEdit
                )
            }
        },
        confirmButton = {
            Button(onClick = { onEvent(ReceivePayVoucherContract.Event.SaveChangesClicked) }) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(ReceivePayVoucherContract.Event.DismissDialog) }) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
fun VoucherItem(
    voucher: ReceivePayVoucher, canEdit: Boolean, onEdit: () -> Unit, onDelete: () -> Unit
) {
    val date = remember(voucher.updatedAt) {
        SimpleDateFormat("yyyy - MM - dd", Locale.getDefault()).format(Date(voucher.updatedAt))
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val language = LocalAppLocale.current

    ConfirmDeleteDialog(show = showDeleteConfirm, onConfirm = {
        showDeleteConfirm = false
        onDelete()
    }, onDismiss = { showDeleteConfirm = false })

    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = voucher.partner.name.displayName(language),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(voucher.partner.type.getStringRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(voucher.transactionType.getStringRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    if (voucher.invoiceId.takeIf { it?.isNotBlank() == true } != null) {
                        Text(
                            text = "#${voucher.invoiceId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
                if (voucher.notes.isNotBlank()) {
                    Text(text = voucher.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f".format(abs(voucher.amount)),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (voucher.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                Text(text = date, style = MaterialTheme.typography.bodySmall)
            }
            if (canEdit) {
                Row(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
}
