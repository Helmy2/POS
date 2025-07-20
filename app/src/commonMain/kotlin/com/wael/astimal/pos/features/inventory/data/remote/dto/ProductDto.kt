package com.wael.astimal.pos.features.inventory.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("ar_name")
    val arName: String?,
    @SerialName("en_name")
    val enName: String,
    val barcode: String?,
    @SerialName("purchase_price")
    val purchasePrice: Double,
    @SerialName("selling_price")
    val sellingPrice: Double,
    @SerialName("average_purchase_price")
    val averagePurchasePrice: Double,
    @SerialName("category_id")
    val categoryId: String?,
    @SerialName("main_unit_id")
    val mainUnitId: String,
    @SerialName("sub_unit_id")
    val subUnitId: String?,
    @SerialName("sub_units_per_main_unit")
    val subUnitsPerMainUnit: Double,
)

fun ProductDto.toEntity(): ProductEntity {
    return ProductEntity(
        localId = id,
        arName = arName,
        enName = enName,
        barcode = barcode,
        purchasePrice = purchasePrice,
        sellingPrice = sellingPrice,
        averagePurchasePrice = averagePurchasePrice,
        categoryId = categoryId,
        mainUnitId = mainUnitId,
        subUnitId = subUnitId,
        subUnitsPerMainUnit = subUnitsPerMainUnit,
        createdAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = updatedAt.parseIsoTimestamp() ?: Clock.now(),
        isSynced = true
    )
}