package com.wael.astimal.pos.features.management.presentation.employee_account


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
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
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.SearchBarWithBackButton
import com.wael.astimal.pos.core.presentation.compoenents.TextInputField
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
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
    Screen(
        loading = state.loading, eventFlow = eventFlow,
        topBar = {
            SearchBarWithBackButton(
                query = state.query,
                onBack = onBack,
                onQueryChange = { onEvent(EmployeeAccountEvent.UpdateQuery(it)) },
                onSearch = { onEvent(EmployeeAccountEvent.UpdateQuery(it)) },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                state.canEdit
            ) {
                FloatingActionButton(
                    onClick = { onEvent(EmployeeAccountEvent.OpenNewTransaction) },
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_partner),
                    )
                }
            }
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionList(
                transactions = state.transactions,
                canEdit = state.canEdit,
                onEvent = onEvent
            )
        }
    }

    if (state.showEditDialog) {
        EditTransactionDialog(
            state, onEvent
        )
    }
}

@Composable
fun EditTransactionDialog(
    state: EmployeeAccountState, onEvent: (EmployeeAccountEvent) -> Unit
) {
    val context = LocalContext.current
    val language = LocalAppLocale.current
    Dialog(onDismissRequest = {
        onEvent(EmployeeAccountEvent.DismissEditDialog)
    }) {
        Card {
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)
            ) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.employee),
                    items = state.employees,
                    selectedItemId = state.selectedEmployee?.id,
                    onItemSelected = {
                        it?.let {
                            onEvent(
                                EmployeeAccountEvent.SelectEmployee(it)
                            )
                        }
                    },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.id },
                    canClearSelection = true
                )

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
                    onClick = { onEvent(EmployeeAccountEvent.SaveTransaction) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_payment))
                }
            }
        }
    }
}

@Composable
fun TransactionList(
    transactions: List<EmployeeAccountTransaction>,
    canEdit: Boolean,
    onEvent: (EmployeeAccountEvent) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        items(transactions) { transaction ->
            TransactionItem(
                transaction = transaction,
                canEdit = canEdit,
                onEdit = { onEvent(EmployeeAccountEvent.EditTransactionClicked(transaction)) },
                onDelete = { onEvent(EmployeeAccountEvent.DeleteTransactionClicked(transaction)) })
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
    val language = LocalAppLocale.current
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            transaction.employee?.localizedName?.displayName(language)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(transaction.type.getStringResId()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "%.2f".format(transaction.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.weight(1f))
                AnimatedVisibility(canEdit) {
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
            transaction.notes.takeIf { !it.isNullOrBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            })
    }
}
