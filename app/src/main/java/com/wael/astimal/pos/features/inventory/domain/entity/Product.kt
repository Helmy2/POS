package com.wael.astimal.pos.features.inventory.domain.entity


data class Product(
    val localId: Long,
    val serverId: Int?,
    val localizedName: LocalizedString,
    val openingBalanceQuantity: Double?,
    val category: Category?,
    val store: Store?,
    val averagePrice: Double,
    val sellingPrice: Double,
    val minimumProductUnit: ProductUnit?,
    val maximumProductUnit: ProductUnit,
    val subUnitsPerMainUnit: Double = 1.0,
    val isSynced: Boolean,
    val lastModified: Long
) {
    val defaultUnit = maximumProductUnit
}

