package com.wael.astimal.pos.features.reports.presentation.employee_ledger

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.util.PdfGeneratorEffect
import com.wael.astimal.pos.features.reports.domain.model.EmployeeLedgerEntry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pos.app.generated.resources.Res
import pos.app.generated.resources.amount_owed
import pos.app.generated.resources.amount_paid
import pos.app.generated.resources.apply_filters
import pos.app.generated.resources.balance
import pos.app.generated.resources.closing_balance
import pos.app.generated.resources.date
import pos.app.generated.resources.description
import pos.app.generated.resources.employee_ledger_report
import pos.app.generated.resources.end_date
import pos.app.generated.resources.select_employee
import pos.app.generated.resources.start_date
import pos.app.generated.resources.totals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeLedgerRoute(
    onNavigateBack: () -> Unit,
    viewModel: EmployeeLedgerViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "employee_ledger_${state.selectedEmployee?.name ?: "report"}",
        onFinish = { viewModel.processEvent(EmployeeLedgerContract.Event.PdfGenerationFinished) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.employee_ledger_report)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.processEvent(EmployeeLedgerContract.Event.GeneratePdf) },
                        enabled = state.ledgerEntries.isNotEmpty()
                    ) { Icon(Icons.Default.PictureAsPdf, "Generate PDF") }
                }
            )
        }
    ) { paddingValues ->
        EmployeeLedgerScreen(
            state = state,
            processEvent = viewModel::processEvent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun EmployeeLedgerScreen(
    state: EmployeeLedgerContract.State,
    processEvent: (EmployeeLedgerContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(EmployeeLedgerContract.Event.SetStartDate(it)) },
            onDismiss = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(EmployeeLedgerContract.Event.SetEndDate(it)) },
            onDismiss = { showEndDatePicker = false }
        )
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        CustomExposedDropdownMenu(
            label = stringResource(Res.string.select_employee),
            currentSelection = state.selectedEmployee?.name ?: "",
            items = state.employees,
            onItemSelected = { processEvent(EmployeeLedgerContract.Event.SelectEmployee(it.id)) },
            itemToDisplayString = { it.name },
            modifier = Modifier.padding(8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.startDate.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(Res.string.start_date)) },
                trailingIcon = {
                    IconButton(onClick = {
                        showStartDatePicker = true
                    }) { Icon(Icons.Default.DateRange, null) }
                },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.endDate.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(Res.string.end_date)) },
                trailingIcon = {
                    IconButton(onClick = {
                        showEndDatePicker = true
                    }) { Icon(Icons.Default.DateRange, null) }
                },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { processEvent(EmployeeLedgerContract.Event.ApplyFilters) },
            enabled = state.selectedEmployeeId != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(Res.string.apply_filters)) }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()

        // --- Report Data ---
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LedgerTable(entries = state.ledgerEntries) {
                processEvent(EmployeeLedgerContract.Event.SelectEntry(it))
            }
        }
    }
}

@Composable
private fun LedgerTable(
    entries: List<EmployeeLedgerEntry>,
    onEntryClick: (EmployeeLedgerEntry) -> Unit
) {
    val totalDebit = entries.sumOf { it.debit }
    val totalCredit = entries.sumOf { it.credit }
    val closingBalance = entries.lastOrNull()?.balance ?: 0.0

    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Header
        item {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                Text(
                    stringResource(Res.string.date),
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    stringResource(Res.string.description),
                    modifier = Modifier.weight(2.5f),
                    style = MaterialTheme.typography.titleSmall
                )
                // --- UPDATED HEADERS ---
                Text(
                    stringResource(Res.string.amount_owed),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End
                )
                Text(
                    stringResource(Res.string.amount_paid),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End
                )
                // --- END OF UPDATE ---
                Text(
                    stringResource(Res.string.balance),
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End
                )
            }
            Divider()
        }
        // Rows
        items(entries) { entry ->
            LedgerRow(entry, onEntryClick)
        }
        // Footer
        if (entries.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                // Totals Row
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                    Text(
                        text = stringResource(Res.string.totals),
                        modifier = Modifier.weight(4f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        String.format("%.2f", totalDebit),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        String.format("%.2f", totalCredit),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold
                    )
                    Text("", modifier = Modifier.weight(1.5f))
                }
                // Closing Balance Row
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                    Text(
                        text = stringResource(Res.string.closing_balance),
                        modifier = Modifier.weight(6.5f),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        String.format("%.2f", closingBalance),
                        modifier = Modifier.weight(1.5f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerRow(entry: EmployeeLedgerEntry, onEntryClick: (EmployeeLedgerEntry) -> Unit) {
    val description = stringResource(entry.transactionType.getStringResId())

    Row(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp).clickable {
            onEntryClick(entry)
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            entry.date.toString(),
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            description,
            modifier = Modifier.weight(2.5f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            if (entry.debit > 0) String.format("%.2f", entry.debit) else "",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
        Text(
            if (entry.credit > 0) String.format("%.2f", entry.credit) else "",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
        Text(
            String.format("%.2f", entry.balance),
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}