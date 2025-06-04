package com.wael.astimal.pos.features.inventory.presentation.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExposedDropdownMenuBoxScope.StringSelectionDropdown(
    currentItem: String,
    label: String,
    expanded: Boolean,
    items: List<String>,
    onClose: () -> Unit,
    onItemSelected: (String) -> Unit,
) {
    OutlinedTextField(
        value = currentItem,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable)
    )
    ExposedDropdownMenu(
        expanded = expanded, onDismissRequest = { onClose() }) {
        items.forEach {
            DropdownMenuItem(
                text = { Text(it) },
                onClick = {
                    onItemSelected(it)
                },
            )
        }
    }
}