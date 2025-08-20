package com.wael.astimal.pos.features.reports.presentation.current_stock

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
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.domain.entity.get
import com.wael.astimal.pos.core.presentation.compoenents.AppButton
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.util.PdfGeneratorEffect
import com.wael.astimal.pos.core.util.formate
import com.wael.astimal.pos.features.reports.domain.model.CurrentStockInfo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pos.app.generated.resources.Res
import pos.app.generated.resources.apply_filters
import pos.app.generated.resources.current_quantity
import pos.app.generated.resources.current_stock_report
import pos.app.generated.resources.product_name
import pos.app.generated.resources.select_product
import pos.app.generated.resources.select_store
import pos.app.generated.resources.store_name

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentStockRoute(
    onNavigateBack: () -> Unit, viewModel: CurrentStockViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "current_stock_report",
        onFinish = { viewModel.processEvent(CurrentStockReducer.Event.PdfGenerationFinished) })

    Screen(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.current_stock_report)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.processEvent(CurrentStockReducer.Event.GeneratePdf) },
                        enabled = state.stockList.isNotEmpty()
                    ) { Icon(Icons.Default.PictureAsPdf, "Generate PDF") }
                })
        }) {
        CurrentStockScreen(
            state = state,
            processEvent = viewModel::processEvent,
        )
    }
}

@Composable
fun CurrentStockScreen(
    state: CurrentStockReducer.State,
    processEvent: (CurrentStockReducer.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            StockTable(stockList = state.stockList) {
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
                            processEvent(CurrentStockReducer.Event.SelectProduct(it?.let {
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
                            processEvent(CurrentStockReducer.Event.SelectStore(it?.let {
                                state.stores.getOrNull(
                                    it
                                )
                            }))
                        },
                    )
                    AppButton(
                        onClick = { processEvent(CurrentStockReducer.Event.ApplyFilters) },
                        modifier = Modifier.width(320.dp)
                    ) { Text(stringResource(Res.string.apply_filters)) }

                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun StockTable(
    stockList: List<CurrentStockInfo>, headerContent: @Composable () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            headerContent()
        }
        // Header
        item {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                Text(
                    stringResource(Res.string.product_name),
                    modifier = Modifier.weight(2.5f),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    stringResource(Res.string.store_name),
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    stringResource(Res.string.current_quantity),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider()
        }
        // Rows
        items(stockList) { stockInfo ->
            StockRow(stockInfo)
        }
    }
}

@Composable
private fun StockRow(info: CurrentStockInfo) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            info.product.name.get(),
            modifier = Modifier.weight(2.5f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            info.store.name.get(),
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            info.quantity.formate(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}