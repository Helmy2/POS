package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.domain.entity.Store

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,

    val serverId: Int?,
    val arName: String?,
    val enName: String?,
    val type: StoreType,
    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

enum class StoreType { MAIN, SUB }

fun StoreEntity.toDomain() : Store {
    return Store(
        localId = this.localId,
        serverId = this.serverId,
        name = LocalizedString(
            arName = this.arName ?: "",
            enName = this.enName ?: ""
        ),
        type = this.type,
        isSynced = this.isSynced,
        lastModified = this.lastModified,
        isDeletedLocally = this.isDeletedLocally
    )
}