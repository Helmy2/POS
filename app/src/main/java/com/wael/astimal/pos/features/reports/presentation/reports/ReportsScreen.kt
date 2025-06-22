package com.wael.astimal.pos.features.reports.presentation.reports

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReportsRoute(
    viewModel: ReportsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportsScreen(
        state = state,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun ReportsScreen(
    state: ReportsContract.State,
    onEvent: (ReportsContract.Event) -> Unit,
) {
    Scaffold { paddingValues ->
        ItemGrid(
            list = state.items,
            onItemClick = { reportItem ->
                onEvent(ReportsContract.Event.ReportClicked(reportItem.destination))
            },
            label = { reportItem ->
                Label(
                    stringResource(id = reportItem.label)
                )
            },
            modifier = Modifier
                .padding(8.dp)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize(),
            isSelected = { false }
        )
    }
}
