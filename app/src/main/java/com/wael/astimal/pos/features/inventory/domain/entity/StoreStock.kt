package com.wael.astimal.pos.features.inventory.domain.entity

data class StoreStock(
    val store: Store,
    val product: Product,
    val quantity: Double
)
