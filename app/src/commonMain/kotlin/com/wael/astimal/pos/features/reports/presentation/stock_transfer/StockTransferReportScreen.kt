package com.wael.astimal.pos.features.reports.presentation.stock_transfer

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.domain.entity.get
import com.wael.astimal.pos.core.presentation.compoenents.AppButton
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.util.PdfGeneratorEffect
import com.wael.astimal.pos.core.util.format
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pos.app.generated.resources.Res
import pos.app.generated.resources.all_stores
import pos.app.generated.resources.apply_filters
import pos.app.generated.resources.date
import pos.app.generated.resources.end_date
import pos.app.generated.resources.from_store
import pos.app.generated.resources.start_date
import pos.app.generated.resources.status
import pos.app.generated.resources.stock_transfer_report
import pos.app.generated.resources.to_store
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferReportRoute(
    onNavigateBack: () -> Unit,
    viewModel: StockTransferReportViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "stock_transfer_report",
        onFinish = { viewModel.processEvent(StockTransferReportReducer.Event.PdfGenerationFinished) }
    )

    Screen(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.stock_transfer_report)) },
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
                        onClick = { viewModel.processEvent(StockTransferReportReducer.Event.GeneratePdf) },
                        enabled = state.transfers.isNotEmpty()
                    ) { Icon(Icons.Default.PictureAsPdf, "Generate PDF") }
                }
            )
        }
    ) {
        StockTransferReportScreen(
            state = state,
            processEvent = viewModel::processEvent,
        )
    }
}

@Composable
fun StockTransferReportScreen(
    state: StockTransferReportReducer.State,
    processEvent: (StockTransferReportReducer.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(StockTransferReportReducer.Event.SetStartDate(it)) },
            onDismiss = { showStartDatePicker = false },
            isStartOfDay = true,

        )
    }
    if (showEndDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(StockTransferReportReducer.Event.SetEndDate(it)) },
            onDismiss = { showEndDatePicker = false },
            isStartOfDay = false,
        )
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {


        // --- Report Data ---
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        itemVerticalAlignment = Alignment.Bottom
                    ) {
                        ExposedDropdownMenu(
                            label = stringResource(Res.string.from_store),
                            options = state.stores.map { it.name.get() },
                            initialText = state.selectedFromStore?.name.get(),
                            onItemSelected = {
                                processEvent(StockTransferReportReducer.Event.SelectFromStore(it?.let {
                                    state.stores.getOrNull(
                                        it
                                    )
                                }))
                            },
                        )
                        ExposedDropdownMenu(
                            label = stringResource(Res.string.to_store),
                            options = listOf(stringResource(Res.string.all_stores)) + state.stores.map { it.name.get() },
                            initialText = state.selectedToStore?.name.get(),
                            onItemSelected = {
                                processEvent(StockTransferReportReducer.Event.SelectToStore(it?.let {
                                    state.stores.getOrNull(it)
                                }))
                            }
                        )
                        LabeledTextField(
                            value = state.startDate.format(),
                            onValueChange = {},
                            readOnly = true,
                            enabled = true,
                            label = stringResource(Res.string.start_date),
                            trailingIcon = {
                                IconButton(onClick = {
                                    showStartDatePicker = true
                                }) { Icon(Icons.Default.DateRange, null) }
                            },
                        )
                        LabeledTextField(
                            value = state.endDate.format(),
                            onValueChange = {},
                            readOnly = true,
                            enabled = true,
                            label = stringResource(Res.string.end_date),
                            trailingIcon = {
                                IconButton(onClick = {
                                    showEndDatePicker = true
                                }) { Icon(Icons.Default.DateRange, null) }
                            },
                        )
                        AppButton(
                            onClick = { processEvent(StockTransferReportReducer.Event.ApplyFilters) },
                            modifier = Modifier.width(320.dp)
                        ) { Text(stringResource(Res.string.apply_filters)) }
                        HorizontalDivider()
                    }
                }

                if (state.transfers.isNotEmpty()) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                            Text(
                                stringResource(Res.string.date),
                                modifier = Modifier.weight(1.5f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                stringResource(Res.string.from_store),
                                modifier = Modifier.weight(1.5f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                stringResource(Res.string.to_store),
                                modifier = Modifier.weight(1.5f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                stringResource(Res.string.status),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
                items(state.transfers) { transfer ->
                    TransferRow(transfer)
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun TransferRow(transfer: StockTransfer) {
    val date = Instant.fromEpochMilliseconds(transfer.createdAt)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                date.toString(),
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                transfer.fromStore.name.get(),
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                transfer.toStore.name.get(),
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                transfer.status.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End
            )
        }
    }
}