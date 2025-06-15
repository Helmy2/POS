package com.wael.astimal.pos.features.management.presentation.business_partner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

val PositiveBalanceColor = Color(0xFFE53935)
val NegativeBalanceColor = Color(0xFF43A047)

@Composable
fun BusinessPartnerRoute(
    onBack: () -> Unit,
    viewModel: BusinessPartnerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BusinessPartnerScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        eventFlow = viewModel.eventFlow,
    )
}

@Composable
fun BusinessPartnerScreen(
    state: BusinessPartnerInfoState,
    onEvent: (BusinessPartnerInfoEvent) -> Unit,
    onBack: () -> Unit,
    eventFlow: SharedFlow<UiEvent>,
) {

    SearchScreen(
        eventFlow = eventFlow,
        query = state.query,
        loading = state.loading,
        onBack = onBack,
        onQueryChange = { onEvent(BusinessPartnerInfoEvent.UpdateQuery(it)) },
        onSearch = { onEvent(BusinessPartnerInfoEvent.UpdateQuery(it)) },
    ) {
        BusinessPartnerList(
            partners = state.searchResults, onPartnerClick = { businessPartner ->
                onEvent(BusinessPartnerInfoEvent.SelectBusinessPartner(businessPartner))
            }, selectedPartnerId = state.selectedBusinessPartner?.id
        )
    }


    AnimatedVisibility(state.showDetailDialog) {
        Dialog(
            onDismissRequest = {
                onEvent(BusinessPartnerInfoEvent.DetailBusinessPartner)
            }) {
            Card {
                BusinessPartnerDetailView(state.selectedBusinessPartner!!)
            }
        }
    }
}


@Composable
fun BusinessPartnerList(
    partners: List<BusinessPartner>,
    onPartnerClick: (BusinessPartner) -> Unit,
    selectedPartnerId: String?
) {
    val language = LocalAppLocale.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(partners, key = { it.id }) { partner ->
            ListItem(headlineContent = {
                Text(
                    partner.name.displayName(language), fontWeight = FontWeight.Bold
                )
            }, supportingContent = {
                Column {
                    Text(
                        stringResource(
                            R.string.address_placeholder,
                            partner.address ?: stringResource(R.string.n_a)
                        )
                    )
                    // Show net balance for BOTH, otherwise show the single relevant value.
                    when (partner.type) {
                        PartnerType.BOTH -> {
                            val balanceText = if (partner.netBalance >= 0) {
                                stringResource(
                                    R.string.net_balance_positive, partner.netBalance
                                )
                            } else {
                                stringResource(
                                    R.string.net_balance_negative, abs(partner.netBalance)
                                )
                            }
                            Text(
                                text = balanceText,
                                fontWeight = FontWeight.SemiBold,
                                color = if (partner.netBalance >= 0) PositiveBalanceColor else NegativeBalanceColor
                            )
                        }

                        PartnerType.CLIENT -> {
                            Text(stringResource(R.string.debt, partner.clientDebt.toString()))
                        }

                        else -> {
                            Text(
                                stringResource(
                                    R.string.indebtedness,
                                    partner.supplierIndebtedness.toString()
                                )
                            )
                        }
                    }
                }
            }, trailingContent = {
                PartnerTypeChip(partnerType = partner.type)
            }, modifier = Modifier
                .clickable { onPartnerClick(partner) }
                .background(
                    if (partner.id == selectedPartnerId) MaterialTheme.colorScheme.inversePrimary
                    else Color.Transparent
                ))
        }
    }
}

@Composable
fun BusinessPartnerDetailView(partner: BusinessPartner) {
    val language = LocalAppLocale.current

    Column(
        modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                partner.name.displayName(language), style = MaterialTheme.typography.headlineMedium
            )
            PartnerTypeChip(partnerType = partner.type)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(
                R.string.address_placeholder, partner.address ?: stringResource(R.string.n_a)
            )
        )

        // Display detailed financial breakdown
        if (partner.type == PartnerType.CLIENT || partner.type == PartnerType.BOTH) {
            Text(stringResource(R.string.debt, partner.clientDebt.toString()))
        }
        if (partner.type == PartnerType.SUPPLIER || partner.type == PartnerType.BOTH) {
            Text(stringResource(R.string.indebtedness, partner.supplierIndebtedness.toString()))
        }

        // Show a clear, color-coded Net Balance summary for partners who are both.
        if (partner.type == PartnerType.BOTH) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            val balanceText = if (partner.netBalance >= 0) {
                stringResource(R.string.net_balance_positive, partner.netBalance)
            } else {
                stringResource(R.string.net_balance_negative, abs(partner.netBalance))
            }
            Text(
                text = balanceText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (partner.netBalance >= 0) PositiveBalanceColor else NegativeBalanceColor
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        Text(stringResource(R.string.phones), fontWeight = FontWeight.Bold)
        partner.phones.forEach { phone ->
            Text("- $phone")
        }

        partner.responsibleEmployee?.let {
            Text(
                stringResource(
                    R.string.responsible_employee, it.localizedName.displayName(language)
                )
            )
        }
    }
}

@Composable
fun PartnerTypeChip(partnerType: PartnerType) {
    val text = when (partnerType) {
        PartnerType.CLIENT -> stringResource(R.string.client)
        PartnerType.SUPPLIER -> stringResource(R.string.supplier)
        PartnerType.BOTH -> stringResource(R.string.client_and_supplier)
    }
    SuggestionChip(
        onClick = { /* No action */ },
        label = { Text(text) },
    )
}
