package com.wael.astimal.pos.features.user.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.theme

@Composable
fun ThemeSettingsRow(
    showDialog: Boolean,
    themeMode: ThemeMode,
    onShowDialog: (Boolean) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    LabeledRow(
        label = stringResource(Res.string.theme),
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableText(
                    content = {
                        Row {
                            Text(stringResource(themeMode.resource()))
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Outlined.Edit, contentDescription = null)
                        }
                    },
                    onClick = {
                        onShowDialog(true)
                    }
                )
                DropdownMenu(
                    expanded = showDialog,
                    onDismissRequest = { onShowDialog(false) },
                    modifier = Modifier.Companion,
                    content = {
                        ThemeMode.entries.forEach { themeMode ->
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(themeMode.resource()))
                                },
                                onClick = {
                                    onThemeChange(themeMode)
                                }
                            )
                        }
                    }
                )
            }
        },
        modifier = modifier
    )
}