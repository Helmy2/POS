package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ItemGrid(
    list: List<T>,
    onItemClick: (T) -> Unit,
    label: @Composable (T) -> Unit,
    isSelected: (T) -> Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        list.isNotEmpty(), modifier = modifier
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(list) {
                Card(
                    onClick = { onItemClick(it) },
                    border = if (isSelected(it)) BorderStroke(
                        width = 5.dp,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) else null
                ) {
                    label.invoke(it)
                }
            }
        }
    }
}
