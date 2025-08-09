package com.wael.astimal.pos.features.management.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain


@Entity(
    tableName = "business_partners",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["responsibleEmployeeLocalId"],
        )
    ],
    indices = [Index(value = ["responsibleEmployeeLocalId"])]
)
data class BusinessPartnerEntity(
    @PrimaryKey
    val localId: String,
    var isSynced: Boolean = false,
    val createdAt: Long = Clock.now(),
    val updatedAt: Long = Clock.now(),
    var isDeletedLocally: Boolean = false,
    var isPrivate: Boolean,
    val arName: String,
    val enName: String,
    val phone: String,
    val address: String,
    val type: PartnerType,
    val responsibleEmployeeLocalId: String,
)

data class BusinessPartnerWithDetailsEntity(
    @Embedded val businessPartner: BusinessPartnerEntity,

    @Relation(
        parentColumn = "responsibleEmployeeLocalId",
        entityColumn = "id", entity = UserEntity::class
    ) val responsibleEmployeeUser: UserEntity?,
)

fun BusinessPartnerWithDetailsEntity.toDomain(): BusinessPartner {
    return BusinessPartner(
        id = businessPartner.localId,
        name = LocalizedString(arName = businessPartner.arName, enName = businessPartner.enName),
        address = businessPartner.address,
        phone = businessPartner.phone,
        type = businessPartner.type,
        isSynced = businessPartner.isSynced,
        responsibleEmployee = responsibleEmployeeUser?.toDomain() ?: throw NullPointerException(),
        createdAt = businessPartner.createdAt,
        updatedAt = businessPartner.updatedAt,
        isPrivate = businessPartner.isPrivate
    )
}