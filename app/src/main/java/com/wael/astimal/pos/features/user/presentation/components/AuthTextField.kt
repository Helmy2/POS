package com.wael.astimal.pos.features.user.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun AuthTextField(
    value: String,
    label: String,
    @StringRes error: Int?,
    keyboardOptions: KeyboardOptions,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label) },
        keyboardOptions = keyboardOptions,
        singleLine = true,
        isError = error != null,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .2f),
            unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .2f),
        ),
        supportingText = when {
            error != null -> {
                {
                    Text(
                        text = stringResource(error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            else -> null
        })
}