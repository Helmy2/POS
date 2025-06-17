package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString


data class Product(
    val localizedName: LocalizedString,
    val openingBalanceQuantity: Double?,
    val category: Category,
    val store: Store,
    val averagePrice: Double,
    val sellingPrice: Double,
    val minimumProductUnit: ProductUnit?,
    val maximumProductUnit: ProductUnit,
    val subUnitsPerMainUnit: Double = 1.0,

    override val id: Id,
    override val isSynced: Boolean,
    override val createdAt: Long,
    override val updatedAt: Long
) : Item

