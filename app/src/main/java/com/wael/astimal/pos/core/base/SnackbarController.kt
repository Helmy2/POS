package com.wael.astimal.pos.core.base

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * An event representing a message to be shown in a Snackbar.
 *
 * @param message The text to be displayed.
 * @param action An optional action the user can take.
 */
data class SnackbarEvent(
    val message: StringResource,
    val action: SnackbarAction? = null
)


/**
 * An action that can be attached to a SnackbarEvent.
 *
 * @param name The label for the action button.
 * @param action The suspend function to be executed when the action is clicked.
 */
data class SnackbarAction(
    val name: StringResource,
    val action: suspend () -> Unit
)


/**
 * A singleton controller to send Snackbar events from anywhere in the app,
 * typically from a ViewModel.
 */
object SnackbarController {

    private val _events = Channel<SnackbarEvent>()
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: SnackbarEvent) {
        _events.send(event)
    }
}