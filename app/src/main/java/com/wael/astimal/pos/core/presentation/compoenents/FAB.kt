package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FAB(
    enable: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = {
            if (enable) onClick()
        },
        containerColor = if (enable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (enable) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        content = content
    )
}