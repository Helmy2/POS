package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.clear_selection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomExposedDropdownMenu(
    label: String,
    items: List<T>,
    selectedItemId: Long?,
    onItemSelected: (T) -> Unit,
    itemToDisplayString: @Composable (T) -> String,
    itemToId: (T) -> Long?,
    modifier: Modifier = Modifier,
    onClearItem: () -> Unit = {},
    canClearSelection: Boolean = false,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedItem = items.find { itemToId(it) == selectedItemId }
    val currentSelectionString = selectedItem?.let { itemToDisplayString(it) } ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier
    ) {
        LabeledTextField(
            value = currentSelectionString,
            onValueChange = {},
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.Companion.PrimaryEditable)
                .onFocusEvent {
                    expanded = it.hasFocus
                },

            enabled = enabled,
            readOnly = true,
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(itemToDisplayString(item)) }, onClick = {
                    onItemSelected(item)
                    expanded = false
                })
            }
            if (selectedItem != null && canClearSelection) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.clear_selection)) },
                    onClick = {
                        onClearItem()
                        expanded = false
                    })
            }
        }
    }
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
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier
    ) {
        LabeledTextField(
            value = currentSelection,
            onValueChange = {},
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.Companion.PrimaryEditable)
                .onFocusEvent {
                    expanded = it.hasFocus
                },
            enabled = enabled,
            readOnly = true,
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(itemToDisplayString(item), modifier = Modifier.padding(8.dp))
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                )
            }
        }
    }
}