package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wael.astimal.pos.core.util.convertToString
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.ok
import pos.app.generated.resources.select_date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPicker(
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )
    val selectedDate = selectedDateMillis.convertToString()


    Box(modifier) {
        LabeledTextField(
            value = selectedDate,
            onValueChange = { }, readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange, contentDescription = "Select date"
                    )
                }
            },
            label = stringResource(Res.string.select_date),
            modifier = Modifier
                .clickable {
                    showDatePicker = !showDatePicker
                }
                .fillMaxWidth(),
            enabled = enabled
        )

        AnimatedVisibility(showDatePicker && enabled) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                showDatePicker = false
                                onDateSelected(it)
                            }
                        },
                    ) {
                        Text(
                            text = stringResource(Res.string.ok),
                            modifier = modifier,
                        )
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}