package com.wael.astimal.pos.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
actual fun POSTheme(
    dynamicColor: Boolean,
    content: @Composable (() -> Unit)
) {
    MaterialTheme(content = content)
}