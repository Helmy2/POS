package com.wael.astimal.pos.features.client_management.presentaion.clinet_info

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.client_management.domain.entity.Client
import com.wael.astimal.pos.features.inventory.presentation.components.BackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun ClientInfoRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientInfoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ClientInfoScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClientInfoScreen(
    state: ClientInfoState,
    onEvent: (ClientInfoEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val isKeyboardVisible = WindowInsets.isImeVisible
    Box(
        modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .fillMaxSize()
    ) {
        Column {
            Surface(
                shape = SearchBarDefaults.dockedShape,
                color = SearchBarDefaults.colors().containerColor,
                contentColor = contentColorFor(SearchBarDefaults.colors().containerColor),
                tonalElevation = SearchBarDefaults.TonalElevation,
                shadowElevation = SearchBarDefaults.ShadowElevation,
                modifier = modifier
                    .zIndex(1f)
                    .width(360.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BackButton(
                        onClick = {
                            if (isKeyboardVisible) keyboard?.hide()
                            else onBack()
                        },
                    )
                    SearchBarDefaults.InputField(
                        query = state.query,
                        onQueryChange = {
                            onEvent(ClientInfoEvent.UpdateQuery(it))
                        },
                        onSearch = {
                            onEvent(ClientInfoEvent.UpdateQuery(it))
                        },
                        expanded = true,
                        onExpandedChange = {},
                        placeholder = { Text(stringResource(R.string.search)) },
                        trailingIcon = {
                            IconButton(onClick = {
                                onEvent(ClientInfoEvent.SearchClients(state.query))
                            }) {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedContent(state.loading, modifier = Modifier.padding(8.dp)) { it ->
                if (it) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ClientList(
                        clients = state.searchResults, onClientClick = { client ->
                            onEvent(ClientInfoEvent.SelectClient(client))
                        }, selectedClientId = state.selectedClient?.localId
                    )
                }
            }
        }


        AnimatedVisibility(state.showDetailDialog) {
            Dialog(
                onDismissRequest = {
                    onEvent(ClientInfoEvent.DetailClient)
                }
            ) {
                Card {
                    ClientDetailView(state.selectedClient!!)
                }
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
        items(clients, key = { it.localId }) { client ->
            ListItem(
                headlineContent = { Text(client.clientName.displayName(language)) },
                supportingContent = { Text("Debt: ${client.debt ?: "N/A"} | Address: ${client.address ?: "N/A"}") },
                modifier = Modifier
                    .clickable { onClientClick(client) }
                    .background(if (client.localId == selectedClientId) MaterialTheme.colorScheme.inversePrimary else Color.Transparent))
        }
    }
}


@Composable
fun ClientDetailView(client: Client) {
    val language = LocalAppLocale.current

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            client.clientName.displayName(language), style = MaterialTheme.typography.headlineMedium
        )
        Text("Address: ${client.address ?: "N/A"}")
        Text("Debt: ${client.debt?.toString() ?: "N/A"}")
        Text("Is Supplier: ${if (client.isSupplier) "Yes" else "No"}")
        Text("Phones:")
        client.phones.forEach { phone ->
            Text("- $phone")
        }
        client.responsibleEmployee?.let {
            Text("Responsible Employee: ${it.localizedName.displayName(language)}")
        }
    }
}