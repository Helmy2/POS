package com.wael.astimal.pos.features.client_management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.client_management.domain.entity.Client
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain


@Entity(
    tableName = "clients",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["userId"], unique = true),
    ]
)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Int?,
    val userId: Long,
    val phone1: String?,
    val phone2: String?,
    val phone3: String?,
    val address: String?,
    val debt: Double?,
    val isSupplier: Boolean = false,
    var isSynced: Boolean = false,
    val responsibleEmployeeLocalId: Long? = null,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

data class ClientWithDetailsEntity(
    @Embedded val client: ClientEntity,

    @Relation(
        parentColumn = "userId", entityColumn = "id", entity = UserEntity::class
    ) val clientUser: UserEntity,

    @Relation(
        parentColumn = "responsibleEmployeeLocalId", entityColumn = "id", entity = UserEntity::class
    ) val responsibleEmployeeUser: UserEntity?,
)

fun ClientWithDetailsEntity.toDomain(): Client {
    val clientNameLocalized = LocalizedString(
        arName = this.clientUser.arName, enName = this.clientUser.enName
    )

    return Client(
        localId = this.client.localId,
        serverId = this.client.serverId,
        userId = this.client.userId,
        clientName = clientNameLocalized,
        phones = listOfNotNull(
            this.client.phone1,
            this.client.phone2,
            this.client.phone3,
        ),
        address = this.client.address,
        debt = this.client.debt,
        isSupplier = this.client.isSupplier,
        isSynced = this.client.isSynced,
        lastModified = this.client.lastModified,
        isDeletedLocally = this.client.isDeletedLocally,
        responsibleEmployee = this.responsibleEmployeeUser?.toDomain()
    )
}