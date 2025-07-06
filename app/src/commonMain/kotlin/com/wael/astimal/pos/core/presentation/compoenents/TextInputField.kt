package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    numberOfLines: Int = 1,
    readOnly: Boolean = false,
    label: String? = null,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Companion.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Companion.Default,
) {
    OutlinedTextField(
        modifier = modifier.height(OutlinedTextFieldDefaults.MinHeight),
        value = value,
        enabled = enabled,
        onValueChange = onValueChange,
        minLines = numberOfLines,
        maxLines = numberOfLines,
        readOnly = readOnly,
        label = label?.let { { Text(text = it) } },
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}