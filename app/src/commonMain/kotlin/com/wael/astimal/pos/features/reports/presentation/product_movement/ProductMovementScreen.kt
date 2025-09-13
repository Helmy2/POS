package com.wael.astimal.pos.features.reports.presentation.product_movement

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
import androidx.compose.ui.text.font.FontWeight
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
import com.wael.astimal.pos.core.util.formate
import com.wael.astimal.pos.features.reports.domain.model.ProductMovementEntry
import com.wael.astimal.pos.features.reports.domain.model.ProductMovementGroup
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pos.app.generated.resources.Res
import pos.app.generated.resources.apply_filters
import pos.app.generated.resources.closing_balance
import pos.app.generated.resources.date
import pos.app.generated.resources.description
import pos.app.generated.resources.end_date
import pos.app.generated.resources.product_movement_report
import pos.app.generated.resources.quantity_in
import pos.app.generated.resources.quantity_out
import pos.app.generated.resources.running_balance
import pos.app.generated.resources.select_product
import pos.app.generated.resources.select_store
import pos.app.generated.resources.start_date
import pos.app.generated.resources.totals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductMovementRoute(
    onNavigateBack: () -> Unit,
    viewModel: ProductMovementViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "product_movement_report",
        onFinish = { viewModel.processEvent(ProductMovementReducer.Event.PdfGenerationFinished) })

    Screen(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.product_movement_report)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.processEvent(ProductMovementReducer.Event.GeneratePdf) },
                        enabled = state.movementGroups.isNotEmpty()
                    ) { Icon(Icons.Default.PictureAsPdf, "Generate PDF") }
                })
        }) {
        ProductMovementScreen(
            state = state,
            processEvent = viewModel::processEvent,
        )
    }
}

@Composable
fun ProductMovementScreen(
    state: ProductMovementReducer.State,
    processEvent: (ProductMovementReducer.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(ProductMovementReducer.Event.SetStartDate(it)) },
            onDismiss = { showStartDatePicker = false },
            isStartOfDay = true
        )
    }
    if (showEndDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(ProductMovementReducer.Event.SetEndDate(it)) },
            onDismiss = { showEndDatePicker = false },
            isStartOfDay = false
        )
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        // --- Report Data ---
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        itemVerticalAlignment = Alignment.Bottom
                    ) {
                        ExposedDropdownMenu(
                            label = stringResource(Res.string.select_product),
                            options = state.products.map { it.name.get() },
                            initialText = state.selectedProduct?.name.get(),
                            onItemSelected = {
                                processEvent(ProductMovementReducer.Event.SelectProduct(it?.let {
                                    state.products.getOrNull(
                                        it
                                    )
                                }))
                            },
                        )
                        ExposedDropdownMenu(
                            label = stringResource(Res.string.select_store),
                            options = state.stores.map { it.name.get() },
                            initialText = state.selectedStore?.name.get(),
                            onItemSelected = {
                                processEvent(ProductMovementReducer.Event.SelectStore(it?.let {
                                    state.stores.getOrNull(
                                        it
                                    )
                                }))
                            },
                        )
                        LabeledTextField(
                            value = state.startDate.format(),
                            onValueChange = {},
                            readOnly = true,
                            label = stringResource(Res.string.start_date),
                            trailingIcon = {
                                IconButton(onClick = {
                                    showStartDatePicker = true
                                }) { Icon(Icons.Default.DateRange, null) }
                            },
                            enabled = true
                        )
                        LabeledTextField(
                            value = state.endDate.format(),
                            onValueChange = {},
                            readOnly = true,
                            label = stringResource(Res.string.end_date),
                            trailingIcon = {
                                IconButton(onClick = {
                                    showEndDatePicker = true
                                }) { Icon(Icons.Default.DateRange, null) }
                            },
                            enabled = true
                        )
                        AppButton(
                            onClick = { processEvent(ProductMovementReducer.Event.ApplyFilters) },
                            modifier = Modifier.width(320.dp)
                        ) { Text(stringResource(Res.string.apply_filters)) }
                        HorizontalDivider()
                    }
                }
                items(state.movementGroups) { group ->
                    ProductMovementGroupView(group = group)
                }
            }
        }
    }
}

@Composable
private fun ProductMovementGroupView(group: ProductMovementGroup) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Product Header
            Text(
                text = group.productName.get(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            HorizontalDivider()

            // Table Header
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
                Text(
                    stringResource(Res.string.quantity_in),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End
                )
                Text(
                    stringResource(Res.string.quantity_out),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End
                )
                Text(
                    stringResource(Res.string.running_balance),
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider()

            // Table Rows for this product
            group.entries.forEach { entry ->
                MovementRow(entry)
            }

            // Table Footer for this product
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                Text(
                    stringResource(Res.string.totals),
                    modifier = Modifier.weight(4f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    group.totalIn.formate(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    group.totalOut.formate(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                )
                Text("", modifier = Modifier.weight(1.5f))
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                Text(
                    stringResource(Res.string.closing_balance),
                    modifier = Modifier.weight(6.5f),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    group.closingBalance.formate(),
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MovementRow(entry: ProductMovementEntry) {
    val description = when {
        entry.productName.get()
            .isNotBlank() -> "${entry.productName.get()} (${entry.storeName.get()})"

        else -> entry.reason.name
    }
    Row(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
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
            if (entry.quantityIn > 0) entry.quantityIn.formate() else "",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
        Text(
            if (entry.quantityOut > 0) entry.quantityOut.formate() else "",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
        Text(
            entry.balance.formate(),
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}

