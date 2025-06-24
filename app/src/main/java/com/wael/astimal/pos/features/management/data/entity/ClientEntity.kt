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
    tableName = "clients",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["responsibleEmployeeLocalId"],
        )
    ],
    indices = [Index(value = ["responsibleEmployeeLocalId"])]
)
data class ClientEntity(
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
    val debt: Double,
    val isSupplier: Boolean = false,
    val responsibleEmployeeLocalId: Long,
) : ItemEntity

data class ClientWithDetailsEntity(
    @Embedded val client: ClientEntity,

    @Relation(
        parentColumn = "responsibleEmployeeLocalId", entityColumn = "id", entity = UserEntity::class
    ) val responsibleEmployeeUser: UserEntity?,
)

fun ClientWithDetailsEntity.toDomain(): BusinessPartner {
    return BusinessPartner(
        clientId = Id(client.localId, client.serverId),
        supplierId = null,
        name = LocalizedString(arName = client.arName, enName = client.enName),
        address = client.address,
        phone = client.phone,
        type = PartnerType.CLIENT,
        clientDebt = client.debt,
        isSynced = client.isSynced,
        responsibleEmployee = responsibleEmployeeUser?.toDomain() ?: throw NullPointerException(),
    )
}