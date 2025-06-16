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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current

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
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                canClearSelection = false,
            )

            TextInputField(
                value = state.amount,
                onValueChange = { onEvent(EmployeeAccountEvent.UpdateAmount(it)) },
                label = stringResource(R.string.amount),
                modifier = Modifier.fillMaxWidth()
            )

            TextInputField(
                value = state.notes,
                onValueChange = { onEvent(EmployeeAccountEvent.UpdateNotes(it)) },
                label = stringResource(R.string.notes_optional),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onEvent(EmployeeAccountEvent.SavePayment) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_payment))
            }

            AnimatedVisibility(state.loading.not()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    state.employeeAccount?.let { account ->
                        AccountSummaryCard(account)
                        Spacer(modifier = Modifier.height(16.dp))
                        TransactionList(transactions = account.transactions)
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
fun TransactionList(transactions: List<EmployeeAccountTransaction>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                text = stringResource(R.string.transaction_history),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(transactions) { transaction ->
            TransactionItem(transaction = transaction)
        }
    }
}

@Composable
fun TransactionItem(transaction: EmployeeAccountTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.type.name, style = MaterialTheme.typography.bodyLarge)
            transaction.notes?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(
            text = "%.2f".format(transaction.amount),
            style = MaterialTheme.typography.bodyLarge,
            color = if (transaction.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}