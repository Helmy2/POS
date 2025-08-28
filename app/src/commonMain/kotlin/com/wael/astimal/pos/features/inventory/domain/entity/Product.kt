package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toISOString
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.remote.dto.ProductDto


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
    val id: String,
    val isSynced: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long = Clock.now()
)

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        localId = id,
        arName = name.arName ?: "",
        enName = name.enName ?: "",
        categoryId = category?.id,
        averagePurchasePrice = averagePrice,
        sellingPrice = sellingPrice,
        subUnitId = subProductUnit?.id,
        subUnitsPerMainUnit = subUnitsPerMainUnit,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        barcode = barcode,
        purchasePrice = purchasePrice,
        mainUnitId = mainProductUnit.id
    )
}

fun Product.toDto(): ProductDto {
    return ProductDto(
        id = id,
        arName = name.arName ?: "",
        enName = name.enName ?: "",
        categoryId = category?.id,
        averagePurchasePrice = averagePrice,
        sellingPrice = sellingPrice,
        subUnitId = subProductUnit?.id,
        subUnitsPerMainUnit = subUnitsPerMainUnit,
        createdAt = createdAt.toISOString(),
        updatedAt = updatedAt.toISOString(),
        barcode = barcode,
        purchasePrice = purchasePrice,
        mainUnitId = mainProductUnit.id
    )
}

