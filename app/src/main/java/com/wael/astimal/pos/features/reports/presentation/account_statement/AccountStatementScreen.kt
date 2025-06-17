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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

// Define colors for Debit and Credit for better readability in the statement
val DebitColor = Color(0xFF388E3C)
val CreditColor = Color(0xFFD32F2F)

@Composable
fun AccountStatementRoute(
    onBack: () -> Unit, viewModel: AccountStatementViewModel = koinViewModel()
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
        }) {
        AnimatedContent(
            targetState = state.selectedPartner == null,
            modifier = Modifier.padding(horizontal = 16.dp),
            transitionSpec = { fadeIn() togetherWith fadeOut() }) { isPartnerListVisible ->
            if (isPartnerListVisible) {
                PartnerSelectionView(
                    isLoading = state.isPartnerListLoading,
                    partners = state.partners,
                    onPartnerSelected = { onEvent(AccountStatementEvent.SelectPartner(it)) })
            } else {
                StatementDetailView(
                    isLoading = state.isStatementLoading,
                    transactions = state.transactions,
                    partner = state.selectedPartner
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
                        supportingContent = { Text(partner.address) },
                        modifier = Modifier.clickable { onPartnerSelected(partner) })
                    HorizontalDivider()
                }
            }
        }
    }
}


@Composable
fun StatementDetailView(
    isLoading: Boolean, transactions: List<AccountTransaction>, partner: BusinessPartner?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
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
                if (partner != null) {
                    StatementFooter(
                        finalBalance = transactions.lastOrNull()?.balance
                    )
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
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.date),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Start
        )
        Text(
            stringResource(R.string.description),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(4f)
        )
        Text(
            stringResource(R.string.debit),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1.8f),
            textAlign = TextAlign.End
        )
        Text(
            stringResource(R.string.credit),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1.8f),
            textAlign = TextAlign.End
        )
        Text(
            stringResource(R.string.balance),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun TransactionRow(transaction: AccountTransaction) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yy", Locale.getDefault()) }
    val isOpeningBalance = transaction.transactionType == TransactionType.OPENING_BALANCE

    val description = if (isOpeningBalance) {
        stringResource(R.string.opening_balance)
    } else {
        "${getTransactionTypeString(transaction.transactionType)} #${transaction.invoiceNumber}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isOpeningBalance) MaterialTheme.colorScheme.secondaryContainer.copy(
                    alpha = 0.2f
                ) else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            transaction.date.format(dateFormatter),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Start
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(4f),
            fontWeight = if (isOpeningBalance) FontWeight.Bold else FontWeight.Normal,
            lineHeight = 16.sp
        )
        Text(
            text = if (transaction.debit != 0.0) String.format(
                Locale.US, "%.2f", transaction.debit
            ) else "—",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.8f),
            textAlign = TextAlign.End,
            color = if (transaction.debit != 0.0) DebitColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (transaction.credit != 0.0) String.format(
                Locale.US, "%.2f", transaction.credit
            ) else "—",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.8f),
            textAlign = TextAlign.End,
            color = if (transaction.credit != 0.0) CreditColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = String.format(Locale.US, "%.2f", transaction.balance),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatementFooter(finalBalance: Double?) {
    if (finalBalance == null) return

    val language = LocalAppLocale.current

    val numberFormat =
        remember { NumberFormat.getCurrencyInstance(Locale(language.code, language.country)) }
    val formattedBalance = numberFormat.format(abs(finalBalance))

    val (summaryText, summaryColor) = when {
        finalBalance > 0.01 -> stringResource(
            R.string.balance_summary_negative, formattedBalance
        ) to DebitColor // They Owe You
        finalBalance < -0.01 -> stringResource(
            R.string.balance_summary_positive, formattedBalance
        ) to CreditColor // You Owe Them
        else -> stringResource(R.string.balance_summary_settled) to MaterialTheme.colorScheme.onSurface
    }

    Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 4.dp, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = summaryColor)
            Text(
                text = summaryText,
                style = MaterialTheme.typography.labelLarge,
                color = summaryColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun getTransactionTypeString(type: TransactionType): String {
    return when (type) {
        TransactionType.OPENING_BALANCE -> stringResource(R.string.opening_balance)
        TransactionType.SALE -> stringResource(R.string.sale)
        TransactionType.PURCHASE -> stringResource(R.string.purchase)
        TransactionType.SALE_RETURN -> stringResource(R.string.sale_return)
        TransactionType.PURCHASE_RETURN -> stringResource(R.string.purchase_return)
        TransactionType.PAYMENT_RECEIVED -> stringResource(R.string.payment_received)
        TransactionType.PAYMENT_SENT -> stringResource(R.string.payment_sent)
    }
}
