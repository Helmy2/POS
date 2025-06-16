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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    eventFlow: SharedFlow<UiEvent>
) {

    BackHandler(enabled = state.selectedPartner != null) {
        onEvent(AccountStatementEvent.ClearPartnerSelection)
    }

    Screen(
        loading = state.isPartnerListLoading || state.isStatementLoading,
        eventFlow = eventFlow,
        topBar = {
            AnimatedContent(
                targetState = state.selectedPartner == null,
            ) { isPartnerListVisible ->
                if (isPartnerListVisible) {
                    SearchBarWithBackButton(
                        query = state.searchQuery,
                        onQueryChange = { onEvent(AccountStatementEvent.SearchPartner(it)) },
                        onSearch = { onEvent(AccountStatementEvent.SearchPartner(it)) },
                        onBack = onBack,
                    )
                } else {
                    val title = if (state.selectedPartner == null) {
                        stringResource(R.string.account_statement)
                    } else {
                        state.selectedPartner.name.displayName(LocalAppLocale.current)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { onEvent(AccountStatementEvent.ExportToPdf) }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = stringResource(R.string.export_as_pdf)
                            )
                        }
                    }
                }
            }
        }
    ) {
        AnimatedContent(
            targetState = state.selectedPartner == null,
            modifier = Modifier.padding(horizontal = 16.dp),
            transitionSpec = { fadeIn() togetherWith fadeOut() }
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
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    stickyHeader {
                        TransactionListHeader()
                    }
                    items(transactions) { transaction ->
                        TransactionRow(transaction = transaction)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    }
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.date),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Start
        )
        Text(
            stringResource(R.string.description),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(3f)
        )
        Text(
            stringResource(R.string.debit),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )
        Text(
            stringResource(R.string.credit),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )
        Text(
            stringResource(R.string.balance),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun TransactionRow(transaction: AccountTransaction) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yy", Locale.getDefault()) }
    val isOpeningBalance = transaction.transactionType == TransactionType.OPENING_BALANCE

    // Improved padding and alignment for better readability.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isOpeningBalance) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- DATE ---
        Text(
            text = transaction.date.format(dateFormatter),
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            fontWeight = if (isOpeningBalance) FontWeight.Bold else FontWeight.Normal
        )
        // --- DESCRIPTION ---
        Text(
            text = transaction.description,
            modifier = Modifier.weight(3f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isOpeningBalance) FontWeight.Bold else FontWeight.Normal,
            lineHeight = 18.sp
        )
        // --- DEBIT ---
        Text(
            text = if (transaction.debit != 0.0) String.format(
                Locale.US,
                "%.2f",
                transaction.debit
            ) else "—",
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            color = if (transaction.debit != 0.0) DebitColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = MaterialTheme.typography.labelMedium.fontFamily // Monospaced look for numbers
        )
        // --- CREDIT ---
        Text(
            text = if (transaction.credit != 0.0) String.format(
                Locale.US,
                "%.2f",
                transaction.credit
            ) else "—",
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            color = if (transaction.credit != 0.0) CreditColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = MaterialTheme.typography.labelMedium.fontFamily
        )
        // --- BALANCE ---
        Text(
            text = String.format(Locale.US, "%.2f", transaction.balance),
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold
        )
    }
}
