package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toDateString
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.ok
import pos.app.generated.resources.select_date
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


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
    val selectedDate = selectedDateMillis.toDateString()


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


/**
 * A composable function that provides a sequential date and time picker dialog.
 *
 * It first shows a DatePickerDialog. Once a date is confirmed, it then shows a TimePickerDialog.
 * The final selected date and time are combined into a LocalDateTime object.
 *
 * @param onDateSelected A callback function that is invoked with the final selected
 * LocalDateTime when the user confirms the time.
 * @param onDismiss A callback function that is invoked when the dialog is dismissed
 * at any point in the process (e.g., by tapping outside, pressing the
 * back button, or clicking the "Cancel" button).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun DataPicker(
    initialDateTime: LocalDateTime = Clock.currentLocalDateTime(),
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    // State for the date picker
    val datePickerState = rememberDatePickerState(
        initialSelectedDate = initialDateTime.date.toJavaLocalDate()
    )

    // State for the time picker
    val timePickerState = rememberTimePickerState(
        initialHour = initialDateTime.hour,
        initialMinute = initialDateTime.minute,
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // The actual time picker
                DatePicker(state = datePickerState, modifier = Modifier)
                Spacer(modifier = Modifier.size(16.dp))
                TimePicker(state = timePickerState, modifier = Modifier)

                // Buttons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Dismiss button
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Confirm button
                    TextButton(
                        onClick = {
                            // Retrieve the selected date from the date picker state, defaulting to now if null
                            val selectedDateMillis =
                                datePickerState.selectedDateMillis ?: Clock.now()
                            val instant = Instant.fromEpochMilliseconds(selectedDateMillis)

                            // Get the date part from the instant in the system's timezone
                            val selectedDate =
                                instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

                            // Create the final LocalDateTime object by combining the date and time
                            val selectedDateTime = LocalDateTime(
                                date = selectedDate,
                                time = LocalTime(timePickerState.hour, timePickerState.minute)
                            )

                            // Invoke the callbacks
                            onDateSelected(selectedDateTime)
                            onDismiss()
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}