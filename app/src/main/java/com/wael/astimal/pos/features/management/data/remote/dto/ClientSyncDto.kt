package com.wael.astimal.pos.features.management.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.management.data.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Represents the "data" object within the API response, containing the list
 * of clients and the next sync date.
 */
@Serializable
data class ClientSyncData(
    val clients: List<ClientDto>,
    @SerialName("date")
    val nextSyncDate: String
)

/**
 * Represents a single client object from the API, including its nested data.
 */
@Serializable
data class ClientDto(
    @SerialName("id") val relationId: Long,
    @SerialName("user_id") val partnerId: Long,
    @SerialName("employee_id") val employeeId: Long?,
    @SerialName("address") val address: String?,
    @SerialName("debt") val debt: Double,
    @SerialName("is_supplier") val isSupplier: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("client_data") val clientInfo: ClientInfoData,
    @SerialName("employee_date") val employeeInfo: EmployeeInfoData?
)

/**
 * Represents the nested "client_data" object.
 */
@Serializable
data class ClientInfoData(
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String?
)

/**
 * Represents the nested "employee_date" object.
 */
@Serializable
data class EmployeeInfoData(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String
)

/**
 * Maps a ClientDto from the network to our unified local BusinessPartnerEntity.
 * This is a crucial step to transform the server's model into our app's model.
 */
fun ClientDto.toEntity(): BusinessPartnerEntity {
    return BusinessPartnerEntity(
        serverId = partnerId,
        enName = clientInfo.name,
        arName = clientInfo.name,
        address = address ?: "",
        phone = clientInfo.phone ?: "",
        openingBalance = debt, // Debt is money they owe us, so it's a positive balance.
        createdAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = updatedAt.parseIsoTimestamp() ?: Clock.now(),
        responsibleEmployeeLocalId = employeeId ?: 0L,
        type = if (isSupplier == 1) PartnerType.CLIENT_AND_CAN_BE_SUPPLIER else PartnerType.CLIENT
    )
}
