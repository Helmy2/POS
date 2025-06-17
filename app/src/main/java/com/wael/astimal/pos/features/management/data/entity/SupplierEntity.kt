package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain


@Entity(
    tableName = "suppliers", foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["responsibleEmployeeLocalId"],
        onDelete = ForeignKey.SET_NULL
    )], indices = [Index(value = ["responsibleEmployeeLocalId"])]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override var isDeletedLocally: Boolean = false,

    val arName: String,
    val enName: String,
    val phone: String,
    val address: String,
    val indebtedness: Double,
    val responsibleEmployeeLocalId: Long,
    val isClient: Boolean,
) : ItemEntity

data class SupplierWithDetailsEntity(
    @Embedded val supplier: SupplierEntity, @Relation(
        parentColumn = "responsibleEmployeeLocalId", entityColumn = "id", entity = UserEntity::class
    ) val responsibleEmployeeUser: UserEntity?
)

fun SupplierWithDetailsEntity.toDomain(): Supplier {
    return Supplier(
        id = Id(supplier.localId, supplier.serverId),
        name = LocalizedString(
            arName = supplier.arName, enName = supplier.enName
        ),
        phone = supplier.phone,
        address = supplier.address,
        isAlsoClient = supplier.isClient,
        responsibleEmployee = responsibleEmployeeUser?.toDomain() ?: throw NullPointerException(),
        isSynced = supplier.isSynced,
        updatedAt = supplier.updatedAt,
        createdAt = supplier.createdAt
    )
}
