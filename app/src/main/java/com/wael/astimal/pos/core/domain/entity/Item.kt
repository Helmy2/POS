package com.wael.astimal.pos.core.domain.entity

interface Item {
    val id: Id
    val createdAt: Long
    val updatedAt: Long
    val isSynced: Boolean
}


data class Id(
    val local: Long,
    val server: Long?
)