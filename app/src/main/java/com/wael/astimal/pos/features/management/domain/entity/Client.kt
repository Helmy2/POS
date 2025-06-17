package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.domain.entity.User

data class Client(
    override val id: Id,
    val name: LocalizedString,
    val phone: String,
    val address: String,
    val isSupplier: Boolean,
    val responsibleEmployee: User,
    override val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean,
    override val createdAt: Long,
    override val updatedAt: Long
) : Item
