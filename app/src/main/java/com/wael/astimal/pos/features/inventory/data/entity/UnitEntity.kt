package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,

    val serverId: Int?,
    var arName: String?,
    var enName: String?,
    var rate: Float,

    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)