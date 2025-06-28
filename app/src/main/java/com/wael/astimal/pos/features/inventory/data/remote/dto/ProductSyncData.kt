package com.wael.astimal.pos.features.inventory.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the "data" object within the products API response.
 */
@Serializable
data class ProductSyncData(
    val products: List<ProductDto>, @SerialName("date") val nextSyncDate: String
)

/**
 * Represents a single product object from the API.
 */
@Serializable
data class ProductDto(
    val id: Long,
    @SerialName("opening_balance") val openingBalance: Double?,
    @SerialName("average_price") val averagePrice: Double,
    @SerialName("minimum_price") val minimumPrice: Double,
    @SerialName("category_id") val categoryId: Long,
    @SerialName("minimum_unit_id") val minimumUnitId: Long?,
    @SerialName("maximum_unit_id") val maximumUnitId: Long,
    @SerialName("store_id") val storeId: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("name") val name: String,
    @SerialName("maximum") val maximumUnitCount: Double?,
    @SerialName("minimum") val minimumUnitCount: Double?,
    val translations: List<TranslationDto>
)


fun ProductDto.toEntity(
    categoryId: Long,
    storeId: Long,
    maximumUnitId: Long,
    minimumUnitId: Long?,
): ProductEntity {
    val arName = translations.find { it.locale == "ar" }?.name
    val enName = translations.find { it.locale == "en" }?.name

    return ProductEntity(
        serverId = id,
        localId = 0L,
        arName = arName ?: "",
        enName = enName ?: name,
        categoryId = categoryId,
        maximumUnitId = maximumUnitId,
        minimumUnitId = minimumUnitId,
        storeId = storeId,
        openingBalanceQuantity = openingBalance ?: 0.0,
        subUnitsPerMainUnit = ((minimumUnitCount ?: 1.0) / (maximumUnitCount ?: 1.0)),
        createdAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = updatedAt.parseIsoTimestamp() ?: Clock.now()
    )
}
