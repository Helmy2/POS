package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.cancel
import pos.app.generated.resources.confirm
import pos.app.generated.resources.confirm_delete

@Composable
fun ConfirmDeleteDialog(
    show: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AnimatedVisibility(show) {
        Dialog(
            onDismissRequest = onDismiss,
            content = {
                Card(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.confirm_delete),
                        )
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onDismiss,
                            ) {
                                Text(stringResource(Res.string.cancel))
                            }
                            Button(
                                onClick = onConfirm,
                            ) {
                                Text(stringResource(Res.string.confirm))
                            }
                        }
                    }
                }
            },
        )
    }
}