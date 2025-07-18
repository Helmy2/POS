package com.wael.astimal.pos.features.dashboard.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.columnSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.common.component.rememberLineComponent
import com.patrykandpatrick.vico.multiplatform.common.fill
import com.patrykandpatrick.vico.multiplatform.common.vicoTheme
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.user.presentation.setting.SettingsRoute
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.slf4j.MDC.clear
import pos.app.generated.resources.Res
import pos.app.generated.resources.dashboard
import pos.app.generated.resources.sales_analytics
import pos.app.generated.resources.settings
import pos.app.generated.resources.total_revenue
import pos.app.generated.resources.total_sales
import pos.app.generated.resources.you_have_pending_transfer
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateToStockTransfer: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        onEvent = viewModel::processEvent,
        onNavigateToStockTransfer = onNavigateToStockTransfer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardContract.State,
    onEvent: (DashboardContract.Event) -> Unit,
    onNavigateToStockTransfer: () -> Unit,
) {
    var showSetting by rememberSaveable {
        mutableStateOf(false)
    }

    Screen(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.dashboard)) }, actions = {
                IconButton(onClick = { showSetting = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(Res.string.settings)
                    )
                }
            })
        }
    ) {
        AnimatedVisibility(state.havePendingTransfer) {
            Text(
                text = stringResource(Res.string.you_have_pending_transfer),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        onNavigateToStockTransfer()
                    }.padding(8.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
        TimePeriodSelector(
            selectedPeriod = state.selectedTimePeriod,
            onPeriodSelected = { onEvent(DashboardContract.Event.TimePeriodSelected(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        KpiCards(kpiData = state.kpiData)
        SalesAnalyticsChart(state = state)

        AnimatedVisibility(showSetting) {
            Dialog(onDismissRequest = { showSetting = false }) {
                Card {
                    SettingsRoute()
                }
            }
        }
    }
}

@Composable
fun KpiCards(kpiData: DashboardContract.KpiData) {
    val language = LocalAppLocale.current
    val numberFormat =
        remember { NumberFormat.getCurrencyInstance(Locale(language.code, language.country)) }
    val formattedTotalRevenue = numberFormat.format(abs(kpiData.totalRevenue))

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KpiCard(
            title = stringResource(Res.string.total_revenue),
            value = formattedTotalRevenue,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = stringResource(Res.string.total_sales),
            value = kpiData.totalSalesCount.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun KpiCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun SalesAnalyticsChart(
    state: DashboardContract.State,
) {
    val chartModelProducer = remember { CartesianChartModelProducer() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd") }

    LaunchedEffect(state.salesAnalytics) {
        if (state.salesAnalytics.isNotEmpty()) {
            chartModelProducer.runTransaction {
                columnSeries {
                    series(state.salesAnalytics.map { it.totalRevenue.toFloat() })
                }
            }
        } else {
            chartModelProducer.runTransaction { clear() }
        }
    }

    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.sales_analytics),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        ColumnCartesianLayer.ColumnProvider.series(
                            vicoTheme.columnCartesianLayerColors.map { color ->
                                rememberLineComponent(fill(MaterialTheme.colorScheme.primary))
                            })
                    ),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { _, value, _ ->
                            state.salesAnalytics.getOrNull(value.toInt())?.date?.format(
                                dateFormatter
                            ) ?: " "
                        },
                    )
                ), modelProducer = chartModelProducer, modifier = Modifier.height(200.dp)
            )
        }
    }
}

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier,
    ) {
        TimePeriod.entries.forEachIndexed { index, period ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = TimePeriod.entries.size,
                    baseShape = RoundedCornerShape(12.dp)
                ),
                onClick = { onPeriodSelected(period) },
                selected = period == selectedPeriod,
                label = { Text(text = stringResource(period.getStringRes())) }
            )
        }
    }
}
