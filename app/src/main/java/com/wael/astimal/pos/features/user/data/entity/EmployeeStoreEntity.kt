package com.wael.astimal.pos.features.user.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity

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
    ]
)
data class EmployeeStoreEntity(
    val employeeLocalId: Long,
    val storeLocalId: Long
)
