package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.domain.entity.User

enum class PartnerType {
    CLIENT,
    SUPPLIER,
    CLIENT_AND_CAN_BE_SUPPLIER,
    SUPPLIER_AND_CAN_BE_CLIENT,
}

data class BusinessPartner(
    override val id: Id,

    val name: LocalizedString,
    val address: String,
    val phone: String,
    val responsibleEmployee: User,
    var type: PartnerType,

    val openingBalance: Double,
    override val isSynced: Boolean,
    override val createdAt: Long,
    override val updatedAt: Long
) : Item