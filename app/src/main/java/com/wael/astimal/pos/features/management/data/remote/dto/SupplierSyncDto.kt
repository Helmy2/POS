package com.wael.astimal.pos.features.management.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.management.data.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the "data" object within the suppliers API response.
 */
@Serializable
data class SupplierSyncData(
    val suppliers: List<SupplierDto>,
    @SerialName("date")
    val nextSyncDate: String
)

/**
 * Represents a single supplier object from the API.
 */
@Serializable
data class SupplierDto(
    @SerialName("id") val partnerId: Long,
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String?,
    @SerialName("address") val address: String?,
    @SerialName("indebtedness") val indebtedness: Double,
    @SerialName("responsible_id") val responsibleId: Long?,
    @SerialName("is_client") val isClient: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

/**
 * Maps a SupplierDto from the network to our unified local BusinessPartnerEntity.
 */
fun SupplierDto.toEntity(): BusinessPartnerEntity {
    return BusinessPartnerEntity(
        serverId = partnerId,
        enName = name,
        arName = name,
        address = address ?: "",
        phone = phone ?: "",
        // Indebtedness is money we owe them, so we store it as a negative balance.
        openingBalance = -indebtedness,
        createdAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = updatedAt.parseIsoTimestamp() ?: Clock.now(),
        responsibleEmployeeLocalId = responsibleId ?: 0L,
        type = if (isClient == 1) PartnerType.SUPPLIER_AND_CAN_BE_CLIENT else PartnerType.SUPPLIER
    )
}

