package com.wael.astimal.pos.features.user.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField

@Composable
fun AuthTextField(
    value: String,
    label: String,
    keyboardOptions: KeyboardOptions,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LabeledTextField(
        enabled = true,
        value = value,
        onValueChange = onValueChange,
        label = label,
        keyboardOptions = keyboardOptions,
        modifier = modifier,
    )
}