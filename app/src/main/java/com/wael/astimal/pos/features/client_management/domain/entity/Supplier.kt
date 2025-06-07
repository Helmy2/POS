package com.wael.astimal.pos.features.client_management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString

data class Supplier(
    val localId: Long,
    val serverId: Int?,
    val name: String,
    val phone: String?,
    val address: String?,
    val indebtedness: Double?,
    val isAlsoClient: Boolean,
    val responsibleEmployeeId: Long?,
    val responsibleEmployeeName: LocalizedString?,
    val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean
)