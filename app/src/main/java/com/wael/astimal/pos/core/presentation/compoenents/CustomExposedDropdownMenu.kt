package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import com.wael.astimal.pos.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomExposedDropdownMenu(
    label: String,
    items: List<T>,
    selectedItemId: Long?,
    onItemSelected: (T?) -> Unit,
    itemToDisplayString: (T) -> String,
    itemToId: (T) -> Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedItem = items.find { itemToId(it) == selectedItemId }
    val currentSelectionString = selectedItem?.let { itemToDisplayString(it) } ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        TextInputField(
            value = currentSelectionString,
            onValueChange = {},
            readOnly = true,
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.Companion.PrimaryEditable),
            enabled = enabled,
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToDisplayString(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
            if (selectedItem != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.clear_selection)) },
                    onClick = {
                        onItemSelected(null)
                        expanded = false
                    }
                )
            }
        }
    }
}