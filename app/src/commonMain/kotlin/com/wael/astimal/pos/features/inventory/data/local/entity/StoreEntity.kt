package com.wael.astimal.pos.features.inventory.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.store_type_main
import pos.app.generated.resources.store_type_sub
import pos.app.generated.resources.store_type_unspecified

@Entity(
    tableName = "stores",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
        ),
    ]
)
data class StoreEntity(
    @PrimaryKey val localId: String,
    var isSynced: Boolean = false,
    val createdAt: Long = Clock.now(),
    val updatedAt: Long = Clock.now(),
    var isDeletedLocally: Boolean = false,

    val arName: String,
    val enName: String,
    val address: String,
    val type: StoreType,
    val employeeId: String,
)

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

data class StoreWithDetails(
    @Embedded val store: StoreEntity,

    @Relation(parentColumn = "employeeId", entityColumn = "id")
    val user: UserEntity?,
)

fun StoreWithDetails.toDomain(): Store {
    return Store(
        id = store.localId,
        name = LocalizedString(arName = store.arName, enName = store.enName),
        type = store.type,
        isSynced = store.isSynced,
        updatedAt = store.updatedAt,
        createdAt = store.createdAt,
        address = store.address,
        employee = user!!.toDomain()
    )
}