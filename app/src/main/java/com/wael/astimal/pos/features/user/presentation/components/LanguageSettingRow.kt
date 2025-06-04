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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.entity.Language

@Composable
fun LanguageSettingRow(
    showDialog: Boolean,
    language: Language,
    onShowDialog: (Boolean) -> Unit,
    onLanguageChange: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    LabeledRow(
        label = stringResource(R.string.language),
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableText(
                    content = {
                        Row {
                            Text(stringResource(language.resource()))
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
                        Language.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(stringResource(lang.resource())) },
                                onClick = { onLanguageChange(lang) }
                            )
                        }
                    }
                )
            }
        },
        modifier = modifier
    )
}