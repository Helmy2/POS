package com.wael.astimal.pos.features.inventory.domain.entity


data class Product(
    val localId: Long,
    val serverId: Int?,

    val localizedName: LocalizedString,
    val openingBalanceQuantity: Double?,
    val averagePrice: Double?,
    val sellingPrice: Double?,
    val firstPeriodData: String?,

    val category: Category?,
    val store: Store?,
    val minimumProductUnit: ProductUnit?,
    val maximumProductUnit: ProductUnit?,

    val minimumStockLevel: Int?,
    val maximumStockLevel: Int?,

    val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean
)

