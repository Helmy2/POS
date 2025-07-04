package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.back_button_icon

@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val layoutDirection = LocalLayoutDirection.current

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            when (layoutDirection) {
                LayoutDirection.Ltr -> Icons.Default.ChevronLeft
                LayoutDirection.Rtl -> Icons.Default.ChevronRight
            },
            contentDescription = stringResource(Res.string.back_button_icon)
        )
    }
}