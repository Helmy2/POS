package com.wael.astimal.pos.features.inventory.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.local.entity.CategoryEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Long,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("ar_name")
    val arName: String?,
    @SerialName("en_name")
    val enName: String
)

fun CategoryDto.toEntity(): CategoryEntity {
    return CategoryEntity(
        serverId = this.id,
        arName = this.arName,
        enName = this.enName,
        createdAt = this.createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = this.updatedAt.parseIsoTimestamp() ?: Clock.now()
    )
}