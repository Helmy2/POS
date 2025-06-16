package com.wael.astimal.pos.features.management.presentation.employee_account


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.BackButton
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.TextInputField
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccount
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel


@Composable
fun EmployeeAccountRoute(
    onBack: () -> Unit,
    viewModel: EmployeeAccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EmployeeAccountScreen(
        onBack = onBack,
        state = state,
        onEvent = viewModel::onEvent,
        eventFlow = viewModel.eventFlow
    )
}

@Composable
fun EmployeeAccountScreen(
    onBack: () -> Unit,
    state: EmployeeAccountState,
    onEvent: (EmployeeAccountEvent) -> Unit,
    eventFlow: SharedFlow<UiEvent>,
) {
    val currentLanguage = LocalAppLocale.current

    if (state.showEditDialog && state.transactionToEdit != null) {
        EditTransactionDialog(
            transaction = state.transactionToEdit,
            isSaving = state.isSaving,
            onDismiss = { onEvent(EmployeeAccountEvent.DismissEditDialog) },
            onSave = { onEvent(EmployeeAccountEvent.SaveTransaction(it)) }
        )
    }

    Screen(
        loading = state.loading, eventFlow = eventFlow, topBar = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
            ) {
                BackButton(onBack, Modifier.padding(16.dp))
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.employee),
                    items = state.employees,
                    selectedItemId = state.selectedEmployee?.id,
                    onItemSelected = { employee ->
                        employee?.let { onEvent(EmployeeAccountEvent.SelectEmployee(it)) }
                    },
                    itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
                    itemToId = { it.id },
                    canClearSelection = false,
                )
            }
        }) {

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Admin-only creation form
            AnimatedVisibility(visible = state.isAdmin) {
                AddTransactionForm(state = state, onEvent = onEvent)
            }

            AnimatedVisibility(visible = !state.loading && state.selectedEmployee != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    state.employeeAccount?.let { account ->
                        AccountSummaryCard(account)
                        Spacer(modifier = Modifier.height(16.dp))
                        TransactionList(
                            transactions = account.transactions,
                            state = state,
                            onEvent = onEvent
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AccountSummaryCard(account: EmployeeAccount) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.account_summary),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.current_balance))
                Text(text = "%.2f".format(account.balance))
            }
        }
    }
}

@Composable
fun AddTransactionForm(state: EmployeeAccountState, onEvent: (EmployeeAccountEvent) -> Unit) {
    val context = LocalContext.current
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CustomExposedDropdownMenu(
            label = stringResource(R.string.transaction_type),
            items = EmployeeTransactionType.entries,
            selectedItemId = state.transactionType.ordinal.toLong(),
            onItemSelected = {
                onEvent(
                    EmployeeAccountEvent.SelectTransactionType(
                        it ?: EmployeeTransactionType.SALARY
                    )
                )
            },
            itemToDisplayString = { context.getString(it.getStringResId()) },
            itemToId = { it.ordinal.toLong() },
            canClearSelection = true
        )
        TextInputField(
            value = state.amount,
            onValueChange = { onEvent(EmployeeAccountEvent.UpdateAmount(it)) },
            label = stringResource(R.string.amount)
        )
        TextInputField(
            value = state.notes,
            onValueChange = { onEvent(EmployeeAccountEvent.UpdateNotes(it)) },
            label = stringResource(R.string.notes_optional)
        )
        Button(
            onClick = { onEvent(EmployeeAccountEvent.AddTransaction) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_payment))
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun TransactionList(
    transactions: List<EmployeeAccountTransaction>,
    state: EmployeeAccountState,
    onEvent: (EmployeeAccountEvent) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                text = stringResource(R.string.transaction_history),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(transactions) { transaction ->
            // Determine if the current user can edit this transaction
            val canEdit = state.isAdmin || state.currentUserId == transaction.createdByEmployeeId
            TransactionItem(
                transaction = transaction,
                canEdit = canEdit,
                onEdit = { onEvent(EmployeeAccountEvent.EditTransactionClicked(transaction)) },
                onDelete = { onEvent(EmployeeAccountEvent.DeleteTransactionClicked(transaction)) }
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: EmployeeAccountTransaction,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.type.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                transaction.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "%.2f".format(transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            if (canEdit) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
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
            text = { Text("Are you sure you want to delete this transaction?") },
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
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun EditTransactionDialog(
    transaction: EmployeeAccountTransaction,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (EmployeeAccountTransaction) -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var notes by remember { mutableStateOf(transaction.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())) {
                Text("Edit Transaction", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                TextInputField(value = amount, onValueChange = { amount = it }, label = "Amount")
                TextInputField(value = notes, onValueChange = { notes = it }, label = "Notes")
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") }
                    Button(onClick = {
                        onSave(
                            transaction.copy(
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                notes = notes
                            )
                        )
                    }, enabled = !isSaving) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
