package com.wael.astimal.pos.features.user.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreEntity

@Entity(
    tableName = "employee_stores",
    primaryKeys = ["employeeLocalId", "storeLocalId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeLocalId"],
        ),
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["storeLocalId"],
        )
    ],
    indices = [
        Index(
            value = ["employeeLocalId", "storeLocalId"],
            unique = true
        ),
    ]
)
data class EmployeeStoreEntity(
    val employeeLocalId: Long,
    val storeLocalId: Long
)
