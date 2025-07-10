package com.wael.astimal.pos.features.management.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain


@Entity(
    tableName = "employee_account_transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByEmployeeId"],
        ),
    ],
    indices = [Index(value = ["employeeId"]), Index(value = ["createdByEmployeeId"])]
)
data class EmployeeTransactionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long,
    val serverId: String?,
    val invoiceId: String?,
    val employeeId: Long,
    val createdByEmployeeId: Long,
    val type: EmployeeTransactionType,
    val amount: Double,
    val notes: String?,
    var isSynced: Boolean = false,
    val createdAt: Long = Clock.now(),
    val updatedAt: Long = Clock.now(),
    var isDeletedLocally: Boolean = false,
)


data class EmployeeTransactionWithDetailsEntity(
    @Embedded val transactionEntity: EmployeeTransactionEntity,

    @Relation(
        parentColumn = "createdByEmployeeId", entityColumn = "id", entity = UserEntity::class
    ) val createdByEmployee: UserEntity?,

    @Relation(
        parentColumn = "employeeId", entityColumn = "id", entity = UserEntity::class
    ) val employee: UserEntity?,
)


fun EmployeeTransactionWithDetailsEntity.toDomain(): EmployeeTransaction {
    return EmployeeTransaction(
        id = Id(transactionEntity.localId, serverStringId = transactionEntity.serverId),
        employee = employee?.toDomain() ?: throw NullPointerException(),
        createdByEmployee = createdByEmployee?.toDomain() ?: throw NullPointerException(),
        type = transactionEntity.type,
        amount = transactionEntity.amount,
        notes = transactionEntity.notes,
        createdAt = transactionEntity.createdAt,
        isSynced = transactionEntity.isSynced,
        updatedAt = transactionEntity.updatedAt
    )
}