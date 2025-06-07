package com.wael.astimal.pos.features.client_management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.client_management.domain.entity.Supplier
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.data.entity.UserEntity


@Entity(
    tableName = "suppliers",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["responsibleEmployeeLocalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["responsibleEmployeeLocalId"])]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    val name: String,
    val phone: String?,
    val address: String?,
    val indebtedness: Double?,
    val responsibleEmployeeLocalId: Long?,
    val isClient: Boolean,
    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

data class SupplierWithDetailsEntity(
    @Embedded
    val supplier: SupplierEntity,
    @Relation(
        parentColumn = "responsibleEmployeeLocalId",
        entityColumn = "id",
        entity = UserEntity::class
    )
    val responsibleEmployeeUser: UserEntity?
)

fun SupplierWithDetailsEntity.toDomain(): Supplier {
    val employeeName = this.responsibleEmployeeUser?.let {
        LocalizedString(arName = it.arName, enName = it.enName ?: it.name)
    }
    return Supplier(
        localId = this.supplier.localId,
        serverId = this.supplier.serverId,
        name = this.supplier.name,
        phone = this.supplier.phone,
        address = this.supplier.address,
        indebtedness = this.supplier.indebtedness,
        isAlsoClient = this.supplier.isClient,
        responsibleEmployeeId = this.supplier.responsibleEmployeeLocalId,
        responsibleEmployeeName = employeeName,
        isSynced = this.supplier.isSynced,
        lastModified = this.supplier.lastModified,
        isDeletedLocally = this.supplier.isDeletedLocally
    )
}
