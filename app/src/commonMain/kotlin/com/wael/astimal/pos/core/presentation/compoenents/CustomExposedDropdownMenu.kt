package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomExposedDropdownMenu(
    label: String,
    items: List<T>,
    selectedItemId: String?,
    onItemSelected: (T) -> Unit,
    itemToDisplayString: @Composable (T) -> String,
    itemToId: (T) -> String?,
    modifier: Modifier = Modifier,
    onClearItem: () -> Unit = {},
    enabled: Boolean = true,
) {
    val selectedItem = items.find { itemToId(it) == selectedItemId }
    val currentSelectionString = selectedItem?.let { itemToDisplayString(it) } ?: ""

    ExposedDropdownMenu(
        options = items.map { itemToDisplayString(it) },
        onItemSelected = { onItemSelected(items[it]) },
        onNoSelection = onClearItem,
        noSelectionText = label,
        modifier = modifier,
        initialText = currentSelectionString,
        enabled = enabled,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomExposedDropdownMenu(
    label: String,
    currentSelection: String,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    itemToDisplayString: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ExposedDropdownMenu(
        options = items.map { itemToDisplayString(it) },
        onItemSelected = { onItemSelected(items[it]) },
        onNoSelection = {},
        noSelectionText = label,
        modifier = modifier,
        initialText = currentSelection,
        enabled = enabled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomExposedDropdownMenu(
    label: String,
    items: List<String>,
    selectedIndex: Int?,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenu(
        options = items,
        onItemSelected = { onItemSelected(it) },
        onNoSelection = {},
        noSelectionText = label,
        modifier = modifier,
        initialText = items.getOrNull(selectedIndex ?: -1) ?: "",
    )
}

@Preview
@Composable
fun ExposedDropdownMenuPrev() {
    ExposedDropdownMenu(
        listOf(
            "Option 1",
            "Option 2",
            "Option 3",
            "Option 4",
            "Option 5",
            "Option 6",
            "Option 7",
            "Option 8",
            "Option 9",
        ),
        onItemSelected = {},
        onNoSelection = {},
        noSelectionText = "No Selection",
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenu(
    options: List<String>,
    onItemSelected: (Int) -> Unit,
    onNoSelection: () -> Unit,
    noSelectionText: String,
    modifier: Modifier = Modifier,
    initialText: String = "",
    enabled: Boolean = true,
    dropDownMaxHeight: Dp = 200.dp
) {
    val focusManager = LocalFocusManager.current

    val textFieldState = rememberTextFieldState(initialText = initialText)

    val filteredOptions = options.filter { it.contains(textFieldState.text, ignoreCase = true) }

    val (allowExpanded, setExpanded) = remember { mutableStateOf(false) }
    val expanded = allowExpanded && filteredOptions.isNotEmpty()


    LaunchedEffect(options) {
        if (options.size == 1) {
            onItemSelected(0)
        }
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = setExpanded,
    ) {
        OutlinedTextField(
            enabled = enabled,
            modifier =
                Modifier.fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                    .onFocusEvent {
                        if (!it.hasFocus) {
                            if (options.contains(textFieldState.text)) {
                                onItemSelected(options.indexOf(textFieldState.text))
                            } else {
                                onNoSelection()
                                textFieldState.setTextAndPlaceCursorAtEnd("")
                            }
                        }
                    },
            state = textFieldState,
            lineLimits = TextFieldLineLimits.SingleLine,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable),
                )
            },
            placeholder = {
                if (options.size == 1) {
                    Text(
                        text = options.first(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } else {
                    Text(
                        text = noSelectionText,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text,
            ),
            onKeyboardAction = {
                filteredOptions.firstOrNull()?.let {
                    textFieldState.setTextAndPlaceCursorAtEnd(it)
                    setExpanded(false)
                } ?: {
                    textFieldState.setTextAndPlaceCursorAtEnd(noSelectionText)
                    setExpanded(false)
                }
                focusManager.clearFocus()
            }
        )
        ExposedDropdownMenu(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = dropDownMaxHeight),
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
        ) {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(option)
                        setExpanded(false)
                        focusManager.clearFocus()
                    },
                    enabled = enabled,
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}