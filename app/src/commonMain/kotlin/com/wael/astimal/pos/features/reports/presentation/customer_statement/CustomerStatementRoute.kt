package com.wael.astimal.pos.features.reports.presentation.customer_statement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.wael.astimal.pos.core.domain.entity.get
import com.wael.astimal.pos.core.presentation.compoenents.AppButton
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.util.PdfGeneratorEffect
import com.wael.astimal.pos.core.util.format
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
        baseFileName = "customer_statement",
        onFinish = { viewModel.processEvent(CustomerStatementReducer.Event.PdfGenerationFinished(it)) }
    )

    Screen(
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
                        onClick = { viewModel.processEvent(CustomerStatementReducer.Event.GeneratePdf) },
                        enabled = state.transactions.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, "Generate PDF")
                    }
                }
            )
        }
    ) {
        CustomerStatementScreen(
            state = state,
            processEvent = viewModel::processEvent,
        )
    }
}

@Composable
fun CustomerStatementScreen(
    state: CustomerStatementReducer.State,
    processEvent: (CustomerStatementReducer.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(CustomerStatementReducer.Event.SetStartDate(it)) },
            onDismiss = { showStartDatePicker = false },
            isStartOfDay = true
        )
    }
    if (showEndDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(CustomerStatementReducer.Event.SetEndDate(it)) },
            onDismiss = { showEndDatePicker = false },
            isStartOfDay = false
        )
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        itemVerticalAlignment = Alignment.Bottom
                    ) {
                        ExposedDropdownMenu(
                            label = stringResource(Res.string.select_customer),
                            options = state.partners.map { it.name.get() },
                            initialText = state.selectedPartner?.name.get(),
                            onItemSelected = {
                                processEvent(
                                    CustomerStatementReducer.Event.SelectPartner(
                                        it?.let { state.partners.getOrNull(it) })
                                )
                            },
                        )
                        LabeledTextField(
                            value = state.startDate.format(),
                            onValueChange = {},
                            readOnly = true,
                            label = stringResource(Res.string.start_date),
                            trailingIcon = {
                                IconButton(onClick = { showStartDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, "Select Start Date")
                                }
                            },
                            enabled = true
                        )
                        LabeledTextField(
                            value = state.endDate.format(),
                            onValueChange = {},
                            readOnly = true,
                            label = stringResource(Res.string.end_date),
                            trailingIcon = {
                                IconButton(onClick = { showEndDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, "Select End Date")
                                }
                            },
                            enabled = true
                        )

                        AppButton(
                            onClick = { processEvent(CustomerStatementReducer.Event.ApplyFilters) },
                            enabled = state.selectedPartner != null,
                            modifier = Modifier.width(320.dp)
                        ) {
                            Text(stringResource(Res.string.apply_filters))
                        }
                        HorizontalDivider()
                    }
                }
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
                        processEvent(CustomerStatementReducer.Event.TransactionClicked(transaction))
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
                transaction.date.format(),
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
