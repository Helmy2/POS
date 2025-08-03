package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pos.app.generated.resources.Res
import pos.app.generated.resources.no_selection

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
        label = label,
        noSelectionText = stringResource(Res.string.no_selection),
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
        label = label,
        noSelectionText = stringResource(Res.string.no_selection),
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
        label = label,
        noSelectionText = stringResource(Res.string.no_selection),
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
        label = "",
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
    label: String,
    modifier: Modifier = Modifier,
    initialText: String = "",
    enabled: Boolean = true,
    dropDownMaxHeight: Dp = 200.dp
) {
    val focusManager = LocalFocusManager.current

    val textFieldState = rememberTextFieldState(initialText = initialText)

    val sortedOptions =
        options.sortedByDescending { it.contains(textFieldState.text, ignoreCase = true) }

    val (expanded, setExpanded) = remember { mutableStateOf(false) }

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
                    Modifier.fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
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
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text,
                ),
                onKeyboardAction = {
                    sortedOptions.firstOrNull()?.let {
                        textFieldState.setTextAndPlaceCursorAtEnd(it)
                        onItemSelected(sortedOptions.indexOf(it))
                        setExpanded(false)
                    } ?: {
                        textFieldState.setTextAndPlaceCursorAtEnd(noSelectionText)
                        onNoSelection()
                        setExpanded(false)
                    }
                    focusManager.clearFocus()
                }
            )
        }
        ExposedDropdownMenu(
            modifier = Modifier
                .heightIn(max = dropDownMaxHeight),
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
        ) {
            sortedOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(option)
                        onItemSelected(sortedOptions.indexOf(option))
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