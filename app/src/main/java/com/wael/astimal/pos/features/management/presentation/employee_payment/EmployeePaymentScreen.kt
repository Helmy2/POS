package com.wael.astimal.pos.features.management.presentation.employee_payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.TextInputField
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import org.koin.androidx.compose.koinViewModel

@Composable
fun EmployeePaymentRoute(
    onBack: () -> Unit,
    viewModel: EmployeePaymentViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.error, state.snackbarMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            viewModel.onEvent(EmployeePaymentEvent.ClearError)
        }
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            viewModel.onEvent(EmployeePaymentEvent.ClearSnackbar)
        }
    }

    EmployeePaymentScreen(onBack = onBack, state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeePaymentScreen(
    state: EmployeePaymentState,
    onEvent: (EmployeePaymentEvent) -> Unit,
    onBack: () -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = state.employees,
            selectedItemId = state.selectedEmployee?.id,
            onItemSelected = { onEvent(EmployeePaymentEvent.SelectEmployee(it)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.transaction_type),
            items = EmployeeTransactionType.entries,
            selectedItemId = state.transactionType.ordinal.toLong(),
            onItemSelected = {
                onEvent(
                    EmployeePaymentEvent.SelectTransactionType(
                        it ?: EmployeeTransactionType.SALARY
                    )
                )
            },
            itemToDisplayString = { context.getString(it.getStringResId()) },
            itemToId = { it.ordinal.toLong() },
        )

        TextInputField(
            value = state.amount,
            onValueChange = { onEvent(EmployeePaymentEvent.UpdateAmount(it)) },
            label = stringResource(R.string.amount),
            modifier = Modifier.fillMaxWidth()
        )

        TextInputField(
            value = state.notes,
            onValueChange = { onEvent(EmployeePaymentEvent.UpdateNotes(it)) },
            label = stringResource(R.string.notes_optional),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onEvent(EmployeePaymentEvent.SavePayment) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_payment))
        }
    }
}
