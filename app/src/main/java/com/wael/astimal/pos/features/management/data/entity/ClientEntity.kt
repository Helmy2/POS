package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain


@Entity(
    tableName = "clients",
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
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int? = null,
    val arName: String,
    val enName: String,
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
        parentColumn = "responsibleEmployeeLocalId", entityColumn = "id", entity = UserEntity::class
    ) val responsibleEmployeeUser: UserEntity?,
)

fun ClientWithDetailsEntity.toDomain(): Client {
    return Client(
        id = this.client.localId,
        name = LocalizedString(
            arName = this.client.arName,
            enName = this.client.enName
        ),
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