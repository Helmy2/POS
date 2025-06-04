package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.features.inventory.data.entity.ProductWithDetailsEntity

data class Product(
    val localId: Long,
    val serverId: Int?,
    val arName: String?,
    val enName: String?,
    val categoryId: Long?,
    val categoryArName: String?,
    val categoryEnName: String?,
    val unitId: Long?,
    val unitArName: String?,
    val unitEnName: String?,
    val averagePrice: Double?,
    val sellingPrice: Double?,
    val openingBalanceQuantity: Double?,
    val storeId: Long?,
    val minimumStockLevel: Int?,
    val minimumStockUnitId: Long?,
    val minimumStockUnitArName: String?,
    val minimumStockUnitEnName: String?,
    val maximumStockLevel: Int?,
    val maximumStockUnitId: Long?,
    val maximumStockUnitArName: String?,
    val maximumStockUnitEnName: String?,
    val firstPeriodData: String?,

    val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean
)

fun ProductWithDetailsEntity.toDomain(): Product {
    return Product(
        localId = product.localId,
        serverId = product.serverId,
        arName = product.arName,
        enName = product.enName,
        categoryId = product.categoryId,
        categoryArName = category?.arName,
        categoryEnName = category?.enName,
        unitId = product.unitId,
        unitArName = unit?.arName,
        unitEnName = unit?.enName,
        averagePrice = product.averagePrice,
        sellingPrice = product.sellingPrice,
        openingBalanceQuantity = product.openingBalanceQuantity,
        storeId = product.storeId,
        minimumStockLevel = product.minimumStockLevel,
        minimumStockUnitId = product.minimumStockUnitId,
        minimumStockUnitArName = minimumStockUnit?.arName,
        minimumStockUnitEnName = minimumStockUnit?.enName,
        maximumStockLevel = product.maximumStockLevel,
        maximumStockUnitId = product.maximumStockUnitId,
        maximumStockUnitArName = maximumStockUnit?.arName,
        maximumStockUnitEnName = maximumStockUnit?.enName,
        firstPeriodData = product.firstPeriodData,
        isSynced = product.isSynced,
        lastModified = product.lastModified,
        isDeletedLocally = product.isDeletedLocally
    )
}
