package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ItemGrid(
    list: List<T>,
    onItemClick: (T) -> Unit,
    label:@Composable (T) -> Unit,
    isSelected: (T) -> Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        list.isNotEmpty(), modifier = modifier
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            list.forEach {
                Card(
                    onClick = { onItemClick(it) }, colors = CardDefaults.cardColors(
                        containerColor = if (isSelected(it)) MaterialTheme.colorScheme.primaryContainer else CardDefaults.cardColors().containerColor
                    )
                ) {
                    label.invoke(it)
                }
            }
        }
    }
}
