package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.domain.entity.User

enum class PartnerType {
    CLIENT,
    SUPPLIER,
    BOTH
}

data class BusinessPartner(
    val clientLocalId: Id?,
    val supplierLocalId: Id?,

    val name: LocalizedString,
    val address: String,
    val phone: String,
    val responsibleEmployee: User,
    var type: PartnerType,

    val clientDebt: Double = 0.0,
    val supplierIndebtedness: Double = 0.0,
    val isSynced: Boolean
) {
    val netBalance: Double get() = supplierIndebtedness - clientDebt
}