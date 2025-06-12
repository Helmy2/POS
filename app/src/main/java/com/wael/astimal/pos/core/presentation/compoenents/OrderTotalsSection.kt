package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.R

@Composable
fun OrderTotalsSection(
    subtotal: Double,
    debt: Double,
    totalAmount: Double,
    amountPaid: Double,
    amountRemaining: Double,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.subtotal), style = MaterialTheme.typography.bodyLarge)
                Text("%.2f".format(subtotal), style = MaterialTheme.typography.bodyLarge)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.previous_debt),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "%.2f".format(debt),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.total_amount),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "%.2f".format(totalAmount),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.paid), style = MaterialTheme.typography.titleMedium)
                Text(
                    "%.2f".format(amountPaid),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.remaining), style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "%.2f".format(amountRemaining),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}