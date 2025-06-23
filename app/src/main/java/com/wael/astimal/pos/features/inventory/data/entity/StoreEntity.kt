package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Store

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,

    val arName: String,
    val enName: String,
    val type: StoreType,
) : ItemEntity

enum class StoreType {
    MAIN, SUB;

    fun getStringResourceId(): Int {
        return when (this) {
            MAIN -> R.string.store_type_main
            SUB -> R.string.store_type_sub
        }
    }
}

fun StoreEntity.toDomain(): Store {
    return Store(
        id = Id(localId, serverId),
        name = LocalizedString(arName = arName, enName = enName),
        type = type,
        isSynced = isSynced,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}