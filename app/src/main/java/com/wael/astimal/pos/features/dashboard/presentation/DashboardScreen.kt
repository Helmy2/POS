package com.wael.astimal.pos.features.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.wael.astimal.pos.R
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = koinViewModel(), snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            viewModel.onEvent(DashboardEvent.ClearError)
        }
    }

    DashboardScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun DashboardScreen(
    state: DashboardState, onEvent: (DashboardEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { KpiCards(kpiData = state.kpiData) }
        item { SalesAnalyticsChart(state = state, onEvent = onEvent) }
    }
}

@Composable
fun KpiCards(kpiData: KpiData) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KpiCard(
            title = stringResource(R.string.total_revenue),
            value = "$${"%,.2f".format(kpiData.totalRevenue)}",
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = stringResource(R.string.todays_sales),
            value = kpiData.todaysSales.toString(),
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
fun SalesAnalyticsChart(state: DashboardState, onEvent: (DashboardEvent) -> Unit) {
    val chartModelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(state.salesAnalytics) {
        state.salesAnalytics.takeIf { it.isNotEmpty() }?.let {
            chartModelProducer.runTransaction {
                columnSeries {
                    series(it.map { it -> it.totalRevenue.toFloat() })
                }
            }
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
                    text = stringResource(R.string.sales_analytics),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            TimePeriodSelector(
                selectedPeriod = state.selectedTimePeriod,
                onPeriodSelected = { onEvent(DashboardEvent.SelectTimePeriod(it)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        ColumnCartesianLayer.ColumnProvider.series(
                            vicoTheme.columnCartesianLayerColors.map { color ->
                                rememberLineComponent(fill(MaterialTheme.colorScheme.primary))
                            }
                        )
                    ),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { _, value, _ ->
                            state.salesAnalytics.getOrNull(value.toInt())?.date?.format(
                                DateTimeFormatter.ofPattern("MMM dd")
                            ) ?: "N/A"
                        },
                    )
                ),
                modelProducer = chartModelProducer,
            )
        }
    }
}

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod, onPeriodSelected: (TimePeriod) -> Unit
) {
    Row {
        TimePeriod.entries.forEach { period ->
            TextButton(
                onClick = { onPeriodSelected(period) },
                enabled = period != selectedPeriod,
            ) {
                Text(text = stringResource(period.getStringRes()))
            }
        }
    }
}
