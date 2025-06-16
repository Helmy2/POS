package com.wael.astimal.pos.features.reports.presentation.account_statement

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.BackButton
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.SearchBarWithBackButton
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.presentation.business_partner.getCompositeId
import com.wael.astimal.pos.features.reports.domain.entity.AccountTransaction
import com.wael.astimal.pos.features.reports.domain.entity.TransactionType
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

// Define colors for Debit and Credit for better readability in the statement
val DebitColor = Color(0xFFD32F2F)
val CreditColor = Color(0xFF388E3C)

@Composable
fun AccountStatementRoute(
    onBack: () -> Unit,
    viewModel: AccountStatementViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountStatementScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        eventFlow = viewModel.eventFlow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountStatementScreen(
    state: AccountStatementState,
    onEvent: (AccountStatementEvent) -> Unit,
    onBack: () -> Unit,
    eventFlow: SharedFlow<UiEvent>,
) {

    BackHandler {
        if (state.selectedPartner == null) {
            onBack()
        } else {
            onEvent(AccountStatementEvent.ClearPartnerSelection)
        }
    }
    Screen(
        loading = state.isStatementLoading,
        eventFlow = eventFlow,
        topBar = {
            val title = if (state.selectedPartner == null) {
                stringResource(R.string.account_statement)
            } else {
                state.selectedPartner.name.displayName(LocalAppLocale.current)
            }

            // The top bar changes depending on the view
            if (state.selectedPartner == null) {
                SearchBarWithBackButton(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(AccountStatementEvent.SearchPartner(it)) },
                    onSearch = { onEvent(AccountStatementEvent.SearchPartner(it)) },
                    onBack = onBack,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BackButton(onClick = { onEvent(AccountStatementEvent.ClearPartnerSelection) })
                    Text(
                        title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    ) {
        AnimatedContent(
            targetState = state.selectedPartner == null,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) { isPartnerListVisible ->
            if (isPartnerListVisible) {
                PartnerSelectionView(
                    isLoading = state.isPartnerListLoading,
                    partners = state.partners,
                    onPartnerSelected = { onEvent(AccountStatementEvent.SelectPartner(it)) }
                )
            } else {
                StatementDetailView(
                    isLoading = state.isStatementLoading,
                    transactions = state.transactions
                )
            }
        }
    }
}

@Composable
fun PartnerSelectionView(
    isLoading: Boolean,
    partners: List<BusinessPartner>,
    onPartnerSelected: (BusinessPartner) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && partners.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(partners, key = { it.getCompositeId() }) { partner ->
                    ListItem(
                        headlineContent = {
                            Text(
                                partner.name.displayName(LocalAppLocale.current),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        supportingContent = { Text(partner.address ?: "") },
                        modifier = Modifier.clickable { onPartnerSelected(partner) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun StatementDetailView(
    isLoading: Boolean,
    transactions: List<AccountTransaction>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item { TransactionListHeader() }
                items(transactions) { transaction ->
                    TransactionRow(transaction = transaction)
                    HorizontalDivider()
                }
            }
        }
    }
}


@Composable
fun TransactionListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.date),
            modifier = Modifier.weight(1.5f),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.description),
            modifier = Modifier.weight(3f),
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.debit),
            modifier = Modifier.weight(1.5f),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
        Text(
            stringResource(R.string.credit),
            modifier = Modifier.weight(1.5f),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
        Text(
            stringResource(R.string.balance),
            modifier = Modifier.weight(2f),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun TransactionRow(transaction: AccountTransaction) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yy", Locale.getDefault()) }
    val isOpeningBalance = transaction.transactionType == TransactionType.OPENING_BALANCE
    val rowModifier = if (isOpeningBalance) {
        Modifier.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
    } else {
        Modifier
    }

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            transaction.date.format(dateFormatter),
            modifier = Modifier.weight(1.5f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Text(
            transaction.description,
            modifier = Modifier.weight(3f),
            fontSize = 14.sp,
            fontWeight = if (isOpeningBalance) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = if (transaction.debit != 0.0) String.format(
                Locale.US,
                "%.2f",
                transaction.debit
            ) else "",
            modifier = Modifier.weight(1.5f),
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            color = DebitColor
        )
        Text(
            text = if (transaction.credit != 0.0) String.format(
                Locale.US,
                "%.2f",
                transaction.credit
            ) else "",
            modifier = Modifier.weight(1.5f),
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            color = CreditColor
        )
        Text(
            text = String.format(Locale.US, "%.2f", transaction.balance),
            modifier = Modifier.weight(2f),
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Medium
        )
    }
}
