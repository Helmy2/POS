package com.wael.astimal.pos.features.inventory.domain.entity


data class UnitDetails(
    val localId: Long,
    val serverId: Int?,
    var arName: String,
    var enName: String,
    val rate: Float,
    val isSynced: Boolean,
    val lastModified: Long,
)