package com.wael.astimal.pos.features.management.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.management.data.local.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessPartnerDto(
    val id: String,
    @SerialName("ar_name") val arName: String,
    @SerialName("en_name") val enName: String,
    val phone: String?,
    val address: String?,
    @SerialName("responsible_employee_id") val responsibleId: String,
    @SerialName("partner_type") val partnerType: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

fun BusinessPartnerDto.toEntity(
    responsibleId: Long
) = BusinessPartnerEntity(
    serverId = id,
    localId = 0L,
    arName = arName,
    enName = enName,
    phone = phone ?: "",
    address = address ?: "",
    type = PartnerType.valueOf(partnerType?.uppercase() ?: PartnerType.CLIENT.name),
    createdAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
    updatedAt = updatedAt.parseIsoTimestamp() ?: Clock.now(),
    responsibleEmployeeLocalId = responsibleId,
    isSynced = true
)