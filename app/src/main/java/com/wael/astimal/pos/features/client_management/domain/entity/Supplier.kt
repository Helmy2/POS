package com.wael.astimal.pos.features.client_management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.domain.entity.User

data class Supplier(
    val id: Long,
    val name: LocalizedString,
    val phone: String?,
    val address: String?,
    val indebtedness: Double?,
    val isAlsoClient: Boolean,
    val responsibleEmployeeName: User?,
    val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean
)