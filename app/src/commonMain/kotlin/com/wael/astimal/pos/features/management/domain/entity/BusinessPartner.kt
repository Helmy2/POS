package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.management.data.local.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.data.remote.dto.BusinessPartnerDto
import com.wael.astimal.pos.features.user.domain.entity.User
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.client
import pos.app.generated.resources.client_and_supplier
import pos.app.generated.resources.supplier

enum class PartnerType {
    CLIENT,
    SUPPLIER,
    BOTH;

    fun getStringRes(): StringResource {
        return when (this) {
            CLIENT -> Res.string.client
            SUPPLIER -> Res.string.supplier
            BOTH -> Res.string.client_and_supplier
        }
    }
}

data class BusinessPartner(
    val id: String,
    val name: LocalizedString,
    val address: String,
    val phone: String,
    val responsibleEmployee: User,
    var type: PartnerType,
    val isSynced: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

fun BusinessPartner.toEntity() = BusinessPartnerEntity(
    localId = id,
    arName = name.arName ?: "",
    enName = name.enName ?: "",
    phone = phone,
    address = address,
    type = type,
    createdAt = createdAt,
    updatedAt = updatedAt,
    responsibleEmployeeLocalId = responsibleEmployee.id
)

fun BusinessPartner.toDto() = BusinessPartnerDto(
    id = id,
    arName = name.arName ?: "",
    enName = name.enName ?: "",
    address = address,
    phone = phone,
    partnerType = type.name.lowercase(),
    createdAt = createdAt.toDateString(),
    updatedAt = updatedAt.toDateString(),
    responsibleId = responsibleEmployee.id
)