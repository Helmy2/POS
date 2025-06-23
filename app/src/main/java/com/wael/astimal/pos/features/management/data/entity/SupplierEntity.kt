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
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain


@Entity(
    tableName = "suppliers", foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["responsibleEmployeeLocalId"],
    )], indices = [Index(value = ["responsibleEmployeeLocalId"])]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
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

fun SupplierWithDetailsEntity.toDomain(): BusinessPartner {
    return BusinessPartner(
        clientLocalId = null,
        supplierLocalId = Id(supplier.localId, supplier.serverId),
        name = LocalizedString(arName = supplier.arName, enName = supplier.enName),
        address = supplier.address,
        phone = supplier.phone,
        type = PartnerType.SUPPLIER,
        supplierIndebtedness = supplier.indebtedness,
        isSynced = supplier.isSynced,
        responsibleEmployee = responsibleEmployeeUser?.toDomain() ?: throw NullPointerException(),
    )
}
