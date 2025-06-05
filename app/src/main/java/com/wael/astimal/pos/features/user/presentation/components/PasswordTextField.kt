package com.wael.astimal.pos.features.user.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.R


@Composable
fun PasswordTextField(
    value: String,
    error: String?,
    isVisible: Boolean,
    supportingText: @Composable (() -> Unit)?,
    keyboardOptions: KeyboardOptions,
    onValueChange: (String) -> Unit,
    onVisibilityToggle: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(stringResource(R.string.password)) },
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = {
                onDone()
            }
        ),
        visualTransformation = if (isVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        singleLine = true,
        isError = error != null,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .2f),
            unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .2f),
        ),
        trailingIcon = {
            PasswordVisibilityToggle(
                isVisible = isVisible, onToggle = onVisibilityToggle
            )
        },
        supportingText = supportingText
    )
}

@Composable
private fun PasswordVisibilityToggle(
    isVisible: Boolean, onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val icon = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff

    IconButton(
        onClick = onToggle, interactionSource = interactionSource, modifier = Modifier.size(24.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isVisible) stringResource(R.string.hide_password) else stringResource(
                R.string.show_password
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
