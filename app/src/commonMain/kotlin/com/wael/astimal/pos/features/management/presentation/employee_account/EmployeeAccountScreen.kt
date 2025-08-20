package com.wael.astimal.pos.features.management.presentation.employee_account


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.wael.astimal.pos.core.domain.entity.get
import com.wael.astimal.pos.core.presentation.compoenents.ConfirmDeleteDialog
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.FAB
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.SearchBarWithBackButton
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.add_transaction
import pos.app.generated.resources.amount
import pos.app.generated.resources.cancel
import pos.app.generated.resources.edit_transaction
import pos.app.generated.resources.employee
import pos.app.generated.resources.new_transaction
import pos.app.generated.resources.notes_optional
import pos.app.generated.resources.save
import pos.app.generated.resources.transaction_type
import kotlin.math.abs


@Composable
fun EmployeeAccountRoute(
    viewModel: EmployeeAccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactionsState.collectAsStateWithLifecycle()

    EmployeeAccountScreen(
        state = state,
        filteredTransactions = filteredTransactions,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun EmployeeAccountScreen(
    state: EmployeeAccountContract.State,
    filteredTransactions: List<EmployeeTransaction>,
    onEvent: (EmployeeAccountContract.Event) -> Unit
) {
    if (state.dialogState.show) {
        EditTransactionDialog(state = state, onEvent = onEvent)
    }

    Screen(topBar = {
        SearchBarWithBackButton(
            query = state.searchQuery,
            onBack = { onEvent(EmployeeAccountContract.Event.NavigateBack) },
            onQueryChange = { onEvent(EmployeeAccountContract.Event.SearchQueryChanged(it)) },
            onSearch = { onEvent(EmployeeAccountContract.Event.SearchQueryChanged(it)) },
            modifier = Modifier.statusBarsPadding()
        )
    }, floatingActionButton = {
        FAB(
            enable = state.canUserEdit,
            onClick = { onEvent(EmployeeAccountContract.Event.AddTransactionClicked) }) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(Res.string.add_transaction),
            )
        }
    }) {
        TransactionList(
            transactions = filteredTransactions, canEdit = state.canUserEdit, onEvent = onEvent
        )
    }
}

@Composable
fun EditTransactionDialog(
    state: EmployeeAccountContract.State, onEvent: (EmployeeAccountContract.Event) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(EmployeeAccountContract.Event.DismissDialog) },
        title = {
            val titleRes =
                if (state.dialogState.selectedTransaction == null) Res.string.new_transaction else Res.string.edit_transaction
            Text(stringResource(titleRes))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenu(
                    label = stringResource(Res.string.employee),
                    options = state.employeesForDropdown.map { it.localizedName.get() },
                    initialText = state.dialogState.selectedEmployee?.localizedName.get(),
                    onItemSelected = {
                        onEvent(
                            EmployeeAccountContract.Event.DialogEmployeeSelected(
                                it?.let {
                                    state.employeesForDropdown.getOrNull(it)
                                }
                            )
                        )
                    },
                    enabled = state.canUserEdit
                )

                ExposedDropdownMenu(
                    label = stringResource(Res.string.transaction_type),
                    options = EmployeeTransactionType.getSelectedList()
                        .map { stringResource(it.getStringResId()) },
                    initialText = state.dialogState.transactionType?.getStringResId()
                        ?.let { stringResource(it) } ?: "",
                    onItemSelected = {
                        onEvent(
                            EmployeeAccountContract.Event.DialogTransactionTypeSelected(
                                it?.let {
                                    EmployeeTransactionType.getSelectedList().getOrNull(it)
                                }
                            )
                        )
                    },
                    enabled = state.canUserEdit
                )
                LabeledTextField(
                    value = state.dialogState.amount,
                    onValueChange = { onEvent(EmployeeAccountContract.Event.DialogAmountChanged(it)) },
                    label = stringResource(Res.string.amount),
                    enabled = state.canUserEdit
                )
                LabeledTextField(
                    value = state.dialogState.notes,
                    onValueChange = { onEvent(EmployeeAccountContract.Event.DialogNotesChanged(it)) },
                    label = stringResource(Res.string.notes_optional),
                    enabled = state.canUserEdit
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(EmployeeAccountContract.Event.SaveChangesClicked) },
                enabled = state.canUserEdit
            ) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(EmployeeAccountContract.Event.DismissDialog) }) {
                Text(stringResource(Res.string.cancel))
            }
        })
}

@Composable
fun TransactionList(
    transactions: List<EmployeeTransaction>,
    canEdit: Boolean,
    onEvent: (EmployeeAccountContract.Event) -> Unit
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Adaptive(320.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(transactions, key = { it.id }) { transaction ->
            TransactionItem(
                transaction = transaction,
                canEdit = canEdit,
                onEdit = { onEvent(EmployeeAccountContract.Event.EditTransactionClicked(transaction)) },
                onDelete = {
                    onEvent(
                        EmployeeAccountContract.Event.DeleteTransactionClicked(
                            transaction
                        )
                    )
                },
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: EmployeeTransaction, canEdit: Boolean, onEdit: () -> Unit, onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val language = LocalAppLocale.current
    Card {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = transaction.employee.localizedName.displayName(language),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(transaction.type.getStringResId()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                transaction.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            AnimatedVisibility(canEdit) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "%.2f".format(abs(transaction.amount)),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp)
                    )
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
    }

    ConfirmDeleteDialog(
        show = showDeleteConfirm,
        onDismiss = { showDeleteConfirm = false },
        onConfirm = {
            onDelete()
            showDeleteConfirm = false
        })
}
