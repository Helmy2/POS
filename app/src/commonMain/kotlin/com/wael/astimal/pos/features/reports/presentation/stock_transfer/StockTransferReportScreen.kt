package com.wael.astimal.pos.features.reports.presentation.stock_transfer

import StockTransfer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.domain.entity.get
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.util.PdfGeneratorEffect
import kotlinx.datetime.Instant
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
        onFinish = { viewModel.processEvent(StockTransferReportContract.Event.PdfGenerationFinished) }
    )

    Scaffold(
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
                        onClick = { viewModel.processEvent(StockTransferReportContract.Event.GeneratePdf) },
                        enabled = state.transfers.isNotEmpty()
                    ) { Icon(Icons.Default.PictureAsPdf, "Generate PDF") }
                }
            )
        }
    ) { paddingValues ->
        StockTransferReportScreen(
            state = state,
            processEvent = viewModel::processEvent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun StockTransferReportScreen(
    state: StockTransferReportContract.State,
    processEvent: (StockTransferReportContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(StockTransferReportContract.Event.SetStartDate(it)) },
            onDismiss = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(StockTransferReportContract.Event.SetEndDate(it)) },
            onDismiss = { showEndDatePicker = false }
        )
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 3,
            modifier = Modifier.fillMaxWidth()
        ) {
            CustomExposedDropdownMenu(
                label = stringResource(Res.string.from_store),
                items = listOf(stringResource(Res.string.all_stores)) + state.stores.map { it.name.get() },
                selectedIndex = state.selectedFromStoreId?.let { id -> state.stores.indexOfFirst { it.id == id } + 1 },
                onItemSelected = { index ->
                    val storeId = if (index == 0) null else state.stores[index - 1].id
                    processEvent(StockTransferReportContract.Event.SelectFromStore(storeId))
                },
            )
            CustomExposedDropdownMenu(
                label = stringResource(Res.string.to_store),
                items = listOf(stringResource(Res.string.all_stores)) + state.stores.map { it.name.get() },
                selectedIndex = state.selectedToStoreId?.let { id -> state.stores.indexOfFirst { it.id == id } + 1 },
                onItemSelected = { index ->
                    val storeId = if (index == 0) null else state.stores[index - 1].id
                    processEvent(StockTransferReportContract.Event.SelectToStore(storeId))
                },
            )
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
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { processEvent(StockTransferReportContract.Event.ApplyFilters) },
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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