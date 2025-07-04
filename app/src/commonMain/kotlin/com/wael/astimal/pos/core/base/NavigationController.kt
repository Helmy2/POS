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
     * @param popUpToRoute The route to pop back to before navigating. Clears the back stack.
     * @param inclusive Whether the `popUpToRoute` should also be popped.
     */
    data class NavigateTo(
        val destination: Destination,
        val popUpToRoute: Destination? = null,
        val inclusive: Boolean = false
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
        popUpToRoute: Destination? = null,
        inclusive: Boolean = false
    ) {
        _events.send(NavigationEvent.NavigateTo(destination, popUpToRoute, inclusive))
    }

    suspend fun navigateBack() {
        _events.send(NavigationEvent.NavigateBack)
    }
}
