package com.wael.astimal.pos.features.management.presentaion.client_info

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
import com.wael.astimal.pos.features.management.domain.entity.Client
import org.koin.androidx.compose.koinViewModel

@Composable
fun ClientInfoRoute(
    onBack: () -> Unit,
    viewModel: ClientInfoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ClientInfoScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@Composable
fun ClientInfoScreen(
    state: ClientInfoState,
    onEvent: (ClientInfoEvent) -> Unit,
    onBack: () -> Unit,
) {

    SearchScreen(
        query = state.query,
        loading = state.loading,
        onBack = onBack,
        onQueryChange = { onEvent(ClientInfoEvent.UpdateQuery(it)) },
        onSearch = { onEvent(ClientInfoEvent.UpdateQuery(it)) },
    ) {
        ClientList(
            clients = state.searchResults, onClientClick = { client ->
                onEvent(ClientInfoEvent.SelectClient(client))
            }, selectedClientId = state.selectedClient?.id
        )
    }


    AnimatedVisibility(state.showDetailDialog) {
        Dialog(
            onDismissRequest = {
                onEvent(ClientInfoEvent.DetailClient)
            }) {
            Card {
                ClientDetailView(state.selectedClient!!)
            }
        }
    }
}

@Composable
fun ClientList(
    clients: List<Client>, onClientClick: (Client) -> Unit, selectedClientId: Long?
) {
    val language = LocalAppLocale.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(clients, key = { it.id }) { client ->
            ListItem(
                headlineContent = { Text(client.name.displayName(language)) },
                supportingContent = {
                    Text(
                        stringResource(
                            R.string.debt_address,
                            client.debt ?: stringResource(R.string.n_a),
                            client.address ?: stringResource(R.string.n_a)
                        )
                    )
                },
                modifier = Modifier
                    .clickable { onClientClick(client) }
                    .background(if (client.id == selectedClientId) MaterialTheme.colorScheme.inversePrimary else Color.Transparent))
        }
    }
}


@Composable
fun ClientDetailView(client: Client) {
    val language = LocalAppLocale.current

    Column(
        modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            client.name.displayName(language), style = MaterialTheme.typography.headlineMedium
        )
        Text(stringResource(R.string.address, client.address ?: stringResource(R.string.n_a)))
        Text(stringResource(R.string.debt, client.debt?.toString() ?: stringResource(R.string.n_a)))
        Text(
            stringResource(
                R.string.is_supplier,
                if (client.isSupplier) stringResource(R.string.yes) else stringResource(
                    R.string.no
                )
            )
        )
        Text(stringResource(R.string.phones))
        client.phones.forEach { phone ->
            Text("- $phone")
        }
        client.responsibleEmployee?.let {
            Text(
                stringResource(
                    R.string.responsible_employee, it.localizedName.displayName(language)
                )
            )
        }
    }
}