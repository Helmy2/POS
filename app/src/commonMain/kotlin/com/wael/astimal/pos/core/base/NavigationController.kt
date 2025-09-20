package com.wael.astimal.pos.core.base

import com.wael.astimal.pos.core.domain.navigation.Destination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A sealed interface representing all possible navigation commands in the app.
 */
sealed interface NavigationEvent {
    /**
     * Navigates to a specific destination.
     * @param destination The target screen.
     */
    data class NavigateTo(
        val destination: Destination,
    ) : NavigationEvent

    data class NavigateToAsRoot(
        val destination: Destination,
    ) : NavigationEvent

    /**
     * Navigates back to the previous screen in the back stack.
     */
    data object NavigateBack : NavigationEvent
}

/**
 * A singleton controller to send navigation events from anywhere in the app,
 * typically from a ViewModel.
 */
object NavigationController {
    private val _events = Channel<NavigationEvent>()
    val events = _events.receiveAsFlow()

    suspend fun navigate(
        destination: Destination,
    ) {
        _events.send(NavigationEvent.NavigateTo(destination))
    }

    suspend fun navigateAsRoot(
        destination: Destination,
    ) {
        _events.send(NavigationEvent.NavigateToAsRoot(destination))
    }

    suspend fun navigateBack() {
        _events.send(NavigationEvent.NavigateBack)
    }
}
