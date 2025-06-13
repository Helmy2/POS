package com.wael.astimal.pos.features.management.presentation.supplier_info

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import org.koin.androidx.compose.koinViewModel

@Composable
fun SupplierInfoRoute(
    onBack: () -> Unit,
    viewModel: SupplierViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SupplierInfoScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@Composable
fun SupplierInfoScreen(
    state: SupplierInfoState,
    onEvent: (SupplierInfoEvent) -> Unit,
    onBack: () -> Unit,
) {
    SearchScreen(
        query = state.query,
        loading = state.loading,
        onBack = onBack,
        onQueryChange = { onEvent(SupplierInfoEvent.UpdateQuery(it)) },
        onSearch = { onEvent(SupplierInfoEvent.UpdateQuery(it)) },
    ) {
        SupplierList(
            suppliers = state.searchResults, onSupplierClick = {
                onEvent(SupplierInfoEvent.SelectSupplier(it))
            }, selectedSupplierId = state.selectedSupplier?.id
        )
    }

    AnimatedVisibility(state.showDetailDialog) {
        Dialog(
            onDismissRequest = {
                onEvent(SupplierInfoEvent.DetailSupplier)
            }) {
            Card {
                SupplierDetailView(state.selectedSupplier!!)
            }
        }
    }
}

@Composable
fun SupplierList(
    suppliers: List<Supplier>, onSupplierClick: (Supplier) -> Unit, selectedSupplierId: Long?
) {
    val language = LocalAppLocale.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(suppliers, key = { it.id }) { supplier ->
            ListItem(
                headlineContent = { Text(supplier.name.displayName(language)) },
                supportingContent = {
                    Text(
                        stringResource(
                            R.string.indebtedness_address,
                            supplier.indebtedness ?: stringResource(R.string.n_a),
                            supplier.address ?: stringResource(R.string.n_a)
                        )
                    )
                },
                modifier = Modifier
                    .clickable { onSupplierClick(supplier) }
                    .background(if (supplier.id == selectedSupplierId) MaterialTheme.colorScheme.inversePrimary else Color.Transparent))
        }
    }
}


@Composable
fun SupplierDetailView(supplier: Supplier) {
    val language = LocalAppLocale.current

    Column(
        modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            supplier.name.displayName(language), style = MaterialTheme.typography.headlineMedium
        )
        Text(stringResource(R.string.address, supplier.address ?: stringResource(R.string.n_a)))
        Text(
            stringResource(
                R.string.is_client,
                if (supplier.isAlsoClient) stringResource(R.string.yes) else stringResource(
                    R.string.no
                )
            )
        )
        Text(stringResource(R.string.phones))
        supplier.phones.forEach { phone ->
            Text("- $phone")
        }
        supplier.responsibleEmployee?.let {
            Text(
                stringResource(
                    R.string.responsible_employee, it.localizedName.displayName(language)
                )
            )
        }
    }
}