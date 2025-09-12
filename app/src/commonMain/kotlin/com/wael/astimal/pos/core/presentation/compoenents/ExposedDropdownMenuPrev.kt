package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.no_selection


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenu(
    options: List<String>,
    onItemSelected: (Int?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    noSelectionText: String = stringResource(Res.string.no_selection),
    initialText: String = "",
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    dropDownMaxHeight: Dp = 200.dp
) {
    val textFieldState = rememberTextFieldState(initialText)

    val sortedOptions = options.sortedByDescending { it.contains(initialText, ignoreCase = true) }


    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    LaunchedEffect(initialText) {
        textFieldState.setTextAndPlaceCursorAtEnd(initialText)
    }

    LaunchedEffect(textFieldState.text) {
        if (initialText == textFieldState.text)
            return@LaunchedEffect
        if (textFieldState.text.isEmpty()) {
            onItemSelected(null)
        } else if (!expanded && !options.any { textFieldState.text == it }) {
            setExpanded(true)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = setExpanded,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(label)
            OutlinedTextField(
                enabled = enabled,
                modifier =
                    Modifier.height(OutlinedTextFieldDefaults.MinHeight)
                        .width(320.dp).menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                state = textFieldState,
                lineLimits = TextFieldLineLimits.SingleLine,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded,
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable),
                    )
                },
                placeholder = {
                    Text(
                        text = noSelectionText,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = imeAction,
                    keyboardType = KeyboardType.Text,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .2f),
                ),
            )
        }
        ExposedDropdownMenu(
            modifier = Modifier
                .heightIn(max = dropDownMaxHeight),
            expanded = expanded && enabled,
            onDismissRequest = { setExpanded(false) },
        ) {
            sortedOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onItemSelected(options.indexOf(option))
                        setExpanded(false)
                    },
                    enabled = enabled,
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}