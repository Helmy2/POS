package com.wael.astimal.pos.features.reports.presentation.client_debit

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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.domain.entity.get
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.util.PdfGeneratorEffect
import com.wael.astimal.pos.features.reports.domain.model.ClientDebitInfo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pos.app.generated.resources.Res
import pos.app.generated.resources.all_employees
import pos.app.generated.resources.apply_filters
import pos.app.generated.resources.client_debit_report
import pos.app.generated.resources.client_name
import pos.app.generated.resources.debit_amount
import pos.app.generated.resources.phone_number
import pos.app.generated.resources.responsible_employee
import pos.app.generated.resources.select_employee
import pos.app.generated.resources.total_debits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDebitRoute(
    onNavigateBack: () -> Unit,
    viewModel: ClientDebitViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "client_debit_report",
        onFinish = { viewModel.processEvent(ClientDebitContract.Event.PdfGenerationFinished) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.client_debit_report)) },
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
                        onClick = { viewModel.processEvent(ClientDebitContract.Event.GeneratePdf) },
                        enabled = state.debitList.isNotEmpty()
                    ) { Icon(Icons.Default.PictureAsPdf, "Generate PDF") }
                }
            )
        }
    ) { paddingValues ->
        ClientDebitScreen(
            state = state,
            processEvent = viewModel::processEvent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun ClientDebitScreen(
    state: ClientDebitContract.State,
    processEvent: (ClientDebitContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        // --- Filter ---
        CustomExposedDropdownMenu(
            label = stringResource(Res.string.select_employee),
            items = listOf(stringResource(Res.string.all_employees)) + state.employees.map { it.name },
            selectedIndex = state.selectedEmployeeId?.let { id -> state.employees.indexOfFirst { it.id == id } + 1 },
            onItemSelected = { index ->
                val employeeId = if (index == 0) null else state.employees[index - 1].id
                processEvent(ClientDebitContract.Event.SelectEmployee(employeeId))
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { processEvent(ClientDebitContract.Event.ApplyFilters) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(Res.string.apply_filters)) }
        Spacer(Modifier.height(16.dp))
        Divider()

        // --- Report Data ---
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            DebitTable(debitList = state.debitList)
        }
    }
}

@Composable
private fun DebitTable(debitList: List<ClientDebitInfo>) {
    val totalDebit = debitList.sumOf { it.debitAmount }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Header
        item {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                Text(
                    stringResource(Res.string.client_name),
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    stringResource(Res.string.phone_number),
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    stringResource(Res.string.responsible_employee),
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    stringResource(Res.string.debit_amount),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End
                )
            }
            Divider()
        }
        // Rows
        items(debitList) { debitInfo ->
            DebitRow(debitInfo)
        }
        // Footer
        if (debitList.isNotEmpty()) {
            item {
                Divider(modifier = Modifier.padding(top = 8.dp))
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                    Text(
                        text = stringResource(Res.string.total_debits),
                        modifier = Modifier.weight(5f),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%.2f", totalDebit),
                        modifier = Modifier.weight(1f),
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
private fun DebitRow(info: ClientDebitInfo) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            info.client.name.get(),
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            info.client.phone ?: "",
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            info.client.responsibleEmployee.name,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            String.format("%.2f", info.debitAmount),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}