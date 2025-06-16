package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.BackButton
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// todo use search screen
@Composable
fun ReceivePayVoucherRoute(
    onBack: () -> Unit,
    viewModel: ReceivePayVoucherViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ReceivePayVoucherScreen(
        onBack = onBack,
        state = state,
        onEvent = viewModel::onEvent,
        eventFlow = viewModel.eventFlow
    )
}

@Composable
fun ReceivePayVoucherScreen(
    state: ReceivePayVoucherState,
    onEvent: (ReceivePayVoucherEvent) -> Unit,
    onBack: () -> Unit,
    eventFlow: SharedFlow<UiEvent>
) {
    val context = LocalContext.current
    val currentLanguage = LocalAppLocale.current

    Screen(
        loading = state.isLoading, eventFlow = eventFlow, topBar = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
            ) {
                BackButton(onBack, Modifier.padding(16.dp))
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.client_or_suppler),
                    items = VoucherPartyType.entries,
                    selectedItemId = state.partyType.ordinal.toLong(),
                    onItemSelected = { it ->
                        it?.let { onEvent(ReceivePayVoucherEvent.SelectPartyType(it)) }
                    },
                    itemToDisplayString = { context.getString(it.getStringRes()) },
                    itemToId = { it.ordinal.toLong() },
                    canClearSelection = false,
                )
            }
        }) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedContent(
                    state.partyType
                ) { partyType ->
                    when (partyType) {
                        VoucherPartyType.CLIENT -> {
                            CustomExposedDropdownMenu(
                                label = stringResource(R.string.client),
                                items = state.clients,
                                selectedItemId = state.selectedClient?.id,
                                onItemSelected = { onEvent(ReceivePayVoucherEvent.SelectClient(it)) },
                                itemToDisplayString = { it.name.displayName(currentLanguage) },
                                itemToId = { it.id },
                                canClearSelection = false,
                            )
                        }

                        VoucherPartyType.SUPPLIER -> {
                            CustomExposedDropdownMenu(
                                label = stringResource(R.string.supplier),
                                items = state.suppliers,
                                selectedItemId = state.selectedSupplier?.id,
                                onItemSelected = { onEvent(ReceivePayVoucherEvent.SelectSupplier(it)) },
                                itemToDisplayString = { it.name.displayName(currentLanguage) },
                                itemToId = { it.id },
                                canClearSelection = false,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { onEvent(ReceivePayVoucherEvent.UpdateAmount(it)) },
                    label = { Text(stringResource(R.string.amount)) },
                    modifier = Modifier.fillMaxWidth()
                )

                DataPicker(
                    selectedDateMillis = state.date, onDateSelected = {
                        onEvent(
                            ReceivePayVoucherEvent.UpdateDate(
                                it ?: System.currentTimeMillis()
                            )
                        )
                    })

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onEvent(ReceivePayVoucherEvent.UpdateNotes(it)) },
                    label = { Text(stringResource(R.string.notes_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { onEvent(ReceivePayVoucherEvent.SaveVoucher) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_voucher))
                }
            }
            VoucherList(vouchers = state.vouchers)
        }
    }
}

@Composable
fun VoucherList(vouchers: List<ReceivePayVoucher>) {
    LazyColumn {
        items(vouchers) { voucher ->
            VoucherItem(voucher = voucher)
        }
    }
}

@Composable
fun VoucherItem(voucher: ReceivePayVoucher) {
    val partyName = when (voucher.party) {
        is Client -> voucher.party.name.displayName(LocalAppLocale.current)
        is Supplier -> voucher.party.name.displayName(LocalAppLocale.current)
        else -> ""
    }
    val date = remember(voucher.date) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(voucher.date))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = partyName, style = MaterialTheme.typography.bodyLarge)
            Text(text = voucher.notes ?: "", style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "%.2f".format(voucher.amount), style = MaterialTheme.typography.bodyLarge
            )
            Text(text = date, style = MaterialTheme.typography.bodySmall)
        }
    }
}
