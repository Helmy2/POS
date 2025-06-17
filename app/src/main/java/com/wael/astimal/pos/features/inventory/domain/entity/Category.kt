package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString

data class Category(
    val localizedName: LocalizedString,
    override val id: Id,
    override val isSynced: Boolean,
    override val createdAt: Long,
    override val updatedAt: Long
) : Item


