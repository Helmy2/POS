package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    numberOfLines: Int = 1,
    readOnly: Boolean = false,
    label: String? = null,
    enabled:Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Companion.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Companion.Default,
    isRtl: Boolean = LocalTextStyle.current.textDirection == TextDirection.Companion.Rtl
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        enabled=enabled,
        onValueChange = onValueChange,
        minLines = numberOfLines,
        maxLines = numberOfLines,
        readOnly = readOnly,
        label = label?.let { { Text(text = it) } },
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = TextStyle(
            textDirection = if (isRtl && value.any { it in '\u0600'..'\u06FF' }) TextDirection.Companion.Rtl else TextDirection.Companion.Ltr
        )
    )
}