package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.domain.entity.Unit

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

fun UnitEntity.toDomain(): Unit {
    return Unit(
        localId = this.localId,
        serverId = this.serverId,
        localizedName = LocalizedString(
            arName = this.arName,
            enName = this.enName
        ),
        rate = this.rate,
        isSynced = this.isSynced,
        lastModified = this.lastModified,
        isDeletedLocally = this.isDeletedLocally
    )
}