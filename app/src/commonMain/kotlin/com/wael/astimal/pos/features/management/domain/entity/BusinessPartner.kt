package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
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
    Both;

    fun getStringRes(): StringResource {
        return when (this) {
            CLIENT -> Res.string.client
            SUPPLIER -> Res.string.supplier
            Both -> Res.string.client_and_supplier
        }
    }
}

data class BusinessPartner(
    override val id: Id,

    val name: LocalizedString,
    val address: String,
    val phone: String,
    val responsibleEmployee: User,
    var type: PartnerType,

    override val isSynced: Boolean,
    override val createdAt: Long,
    override val updatedAt: Long
) : Item

fun BusinessPartner.toEntity() = BusinessPartnerEntity(
    serverId = id.serverStringId,
    localId = id.local,
    arName = name.arName ?: "",
    enName = name.enName ?: "",
    phone = phone,
    address = address,
    type = type,
    createdAt = createdAt,
    updatedAt = updatedAt,
    responsibleEmployeeLocalId = responsibleEmployee.id.local
)

fun BusinessPartner.toDto() = BusinessPartnerDto(
    id = id.serverStringId!!,
    arName = name.arName ?: "",
    enName = name.enName ?: "",
    address = address,
    phone = phone,
    partnerType = type.name.lowercase(),
    createdAt = createdAt.toDateString(),
    updatedAt = updatedAt.toDateString(),
    responsibleId = responsibleEmployee.id.serverStringId!!
)