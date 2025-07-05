package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity


data class Product(
    val name: LocalizedString,
    val category: Category?,
    val averagePrice: Double,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val barcode: String,
    val subProductUnit: ProductUnit?,
    val mainProductUnit: ProductUnit,
    val subUnitsPerMainUnit: Double,
    override val id: Id,
    override val isSynced: Boolean = false,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now()
) : Item

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        localId = id.local,
        serverId = id.server,
        arName = name.arName ?: "",
        enName = name.enName ?: "",
        categoryId = category?.id?.local,
        averagePurchasePrice = averagePrice,
        sellingPrice = sellingPrice,
        subUnitId = subProductUnit?.id?.local,
        subUnitsPerMainUnit = subUnitsPerMainUnit,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        barcode = barcode,
        purchasePrice = purchasePrice,
        mainUnitId = mainProductUnit.id.local
    )
}

