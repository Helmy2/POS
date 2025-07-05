package com.wael.astimal.pos.features.inventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.store_type_main
import pos.app.generated.resources.store_type_sub
import pos.app.generated.resources.store_type_unspecified

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
    val address: String,
    val type: StoreType,
) : ItemEntity

enum class StoreType {
    MAIN, SUB, UNSPECIFIED;

    fun getStringResourceId(): StringResource {
        return when (this) {
            MAIN -> Res.string.store_type_main
            SUB -> Res.string.store_type_sub
            UNSPECIFIED -> Res.string.store_type_unspecified
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
        createdAt = createdAt,
        address = address
    )
}