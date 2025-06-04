package com.wael.astimal.pos.core.domain.navigation

import kotlinx.serialization.Serializable

sealed class Destination {
    val name: String get() = this::class.simpleName ?: "unknown"

    @Serializable
    data object Auth : Destination() {
        @Serializable
        data object Login : Destination()
    }

    @Serializable
    data object Main : Destination() {
        @Serializable
        data object Dashboard : Destination()

        @Serializable
        data object Inventory : Destination()

        @Serializable
        data object Settings : Destination()
    }
}