package com.wael.astimal.pos.features.reports.presentation.customer_statement

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.core.util.PdfGeneratorEffect
import com.wael.astimal.pos.core.util.format
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pos.app.generated.resources.Res
import pos.app.generated.resources.apply_filters
import pos.app.generated.resources.customer_statement
import pos.app.generated.resources.date
import pos.app.generated.resources.description
import pos.app.generated.resources.end_date
import pos.app.generated.resources.select_customer
import pos.app.generated.resources.start_date
import pos.app.generated.resources.total
import pos.app.generated.resources.total_amount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerStatementRoute(
    onNavigateBack: () -> Unit,
    viewModel: CustomerStatementViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "customer_statement_${state.selectedPartner?.name?.enName ?: "report"}",
        onFinish = { viewModel.processEvent(CustomerStatementContract.Event.PdfGenerationFinished(it)) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.customer_statement)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.processEvent(CustomerStatementContract.Event.GeneratePdf) },
                        enabled = state.transactions.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, "Generate PDF")
                    }
                }
            )
        }
    ) { paddingValues ->
        CustomerStatementScreen(
            state = state,
            processEvent = viewModel::processEvent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun CustomerStatementScreen(
    state: CustomerStatementContract.State,
    processEvent: (CustomerStatementContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(CustomerStatementContract.Event.SetStartDate(it)) },
            onDismiss = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(CustomerStatementContract.Event.SetEndDate(it)) },
            onDismiss = { showEndDatePicker = false }
        )
    }

    val language = LocalAppLocale.current
    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {

        CustomExposedDropdownMenu(
            label = stringResource(Res.string.select_customer),
            items = state.partners,
            currentSelection = state.selectedPartner?.name?.displayName(
                language
            ) ?: "",
            onItemSelected = { processEvent(CustomerStatementContract.Event.SelectPartner(it)) },
            itemToDisplayString = { it.name.displayName(language) },
            modifier = Modifier.padding(8.dp),
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.startDate.format(),
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    stringResource(Res.string.start_date),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            trailingIcon = {
                IconButton(onClick = { showStartDatePicker = true }) {
                    Icon(Icons.Default.DateRange, "Select Start Date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.endDate.format(),
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    stringResource(Res.string.end_date),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            trailingIcon = {
                IconButton(onClick = { showEndDatePicker = true }) {
                    Icon(Icons.Default.DateRange, "Select End Date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { processEvent(CustomerStatementContract.Event.ApplyFilters) },
            enabled = state.selectedPartner != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.apply_filters))
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(Res.string.date),
                            modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            stringResource(Res.string.description),
                            modifier = Modifier.weight(3f),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            stringResource(Res.string.total_amount),
                            modifier = Modifier.weight(1.5f),
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
                items(state.transactions) { transaction ->
                    TransactionRow(transaction) {
                        processEvent(CustomerStatementContract.Event.TransactionClicked(transaction))
                    }
                }
                if (state.transactions.isNotEmpty()) {
                    item {
                        HorizontalDivider()
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                stringResource(Res.string.total),
                                modifier = Modifier.weight(2f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "",
                                modifier = Modifier.weight(3f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                state.transactions.sumOf { it.totalAmount }.toString(),
                                modifier = Modifier.weight(1.5f),
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: DetailedTransaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(
            enabled = transaction.transactionType != TransactionType.OPENING_BALANCE
                    && transaction.transactionType != TransactionType.PAYMENT,
            onClick = onClick
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                transaction.date.toString(),
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                stringResource(transaction.transactionType.getStringRes()),
                modifier = Modifier.weight(3f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                transaction.totalAmount.toString(),
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}
