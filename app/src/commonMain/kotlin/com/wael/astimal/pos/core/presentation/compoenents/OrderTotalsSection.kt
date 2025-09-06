package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.presentation.theme.CreditColor
import com.wael.astimal.pos.core.presentation.theme.DebitColor
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.balance_summary_negative
import pos.app.generated.resources.balance_summary_positive
import pos.app.generated.resources.balance_summary_settled
import pos.app.generated.resources.owns_you
import pos.app.generated.resources.paid
import pos.app.generated.resources.partner_current_balance
import pos.app.generated.resources.partner_previous_balance
import pos.app.generated.resources.remaining
import pos.app.generated.resources.total_amount
import pos.app.generated.resources.you_owns
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun OrderTotalsSection(
    totalAmount: Double,
    amountPaid: Double,
    amountRemaining: Double,
    partnerBalance: Double,
    partnerBalanceAfterThisOrder: Double,
    isNew: Boolean
) {
    Card(
        modifier = Modifier.width(320.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(Res.string.total_amount),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "%.2f".format(totalAmount),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(Res.string.paid),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "%.2f".format(amountPaid),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(Res.string.remaining),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "%.2f".format(amountRemaining),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
    Card(
        modifier = Modifier.width(320.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(Res.string.partner_previous_balance),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (partnerBalance > 0.0)
                        stringResource(Res.string.owns_you)
                    else if (partnerBalance < 0.0) stringResource(Res.string.you_owns)
                    else stringResource(Res.string.balance_summary_settled),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(.5f))
                Text(
                    "%.0f".format(abs(partnerBalance)),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider()

            if (isNew) {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(Res.string.partner_current_balance),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        if (partnerBalanceAfterThisOrder > 0.0) stringResource(Res.string.owns_you)
                        else if (partnerBalanceAfterThisOrder < 0.0) stringResource(Res.string.you_owns)
                        else stringResource(Res.string.balance_summary_settled),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(.5f))
                    Text(
                        "%.0f".format(abs(partnerBalanceAfterThisOrder)),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                StatementFooter(partnerBalanceAfterThisOrder)
            }
        }
    }
}


@Composable
fun StatementFooter(finalBalance: Double) {
    val language = LocalAppLocale.current

    val numberFormat =
        remember { NumberFormat.getCurrencyInstance(Locale(language.code, language.country)) }
    val formattedBalance = numberFormat.format(abs(finalBalance))

    val (summaryText, summaryColor) = when {
        finalBalance > 0.01 -> stringResource(
            Res.string.balance_summary_negative, formattedBalance
        ) to DebitColor

        finalBalance < -0.01 -> stringResource(
            Res.string.balance_summary_positive, formattedBalance
        ) to CreditColor

        else -> stringResource(Res.string.balance_summary_settled) to MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = summaryText,
        style = MaterialTheme.typography.labelLarge,
        color = summaryColor,
        fontWeight = FontWeight.Bold
    )
}