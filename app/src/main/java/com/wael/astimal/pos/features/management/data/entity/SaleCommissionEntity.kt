package com.wael.astimal.pos.features.management.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.management.domain.entity.SaleCommission
import com.wael.astimal.pos.features.management.domain.entity.SourceTransactionType
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain

@Entity(
    tableName = "employee_sale_commissions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
        ),
    ],
    indices = [Index(value = ["employeeId"]), Index(value = ["sourceTransactionId", "sourceTransactionType"])]
)
data class SaleCommissionEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override var isDeletedLocally: Boolean = false,
    val employeeId: Long,
    val sourceTransactionId: Long,
    val sourceTransactionType: SourceTransactionType,
    val commissionAmount: Double,
) : ItemEntity

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
        ForeignKey(
            entity = SaleCommissionEntity::class,
            parentColumns = ["localId"],
            childColumns = ["relatedCommissionId"],
        )
    ],
    indices = [Index(value = ["employeeId"]), Index(value = ["createdByEmployeeId"])]
)
data class EmployeeAccountTransactionEntity(
    val employeeId: Long,
    val createdByEmployeeId: Long,
    val type: EmployeeTransactionType,
    val amount: Double,
    @ColumnInfo(index = true) val relatedCommissionId: Long?,
    val notes: String?,
    @PrimaryKey(autoGenerate = true)
    override val localId: Long,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override var isDeletedLocally: Boolean = false,
) : ItemEntity


data class EmployeeAccountTransactionWithDetailsEntity(
    @Embedded
    val transactionEntity: EmployeeAccountTransactionEntity,

    @Relation(
        parentColumn = "createdByEmployeeId",
        entityColumn = "id",
        entity = UserEntity::class
    )
    val createdByEmployee: UserEntity?,

    @Relation(
        parentColumn = "employeeId",
        entityColumn = "id",
        entity = UserEntity::class
    )
    val employee: UserEntity?
)

fun SaleCommissionEntity.toDomain(): SaleCommission {
    return SaleCommission(
        id = Id(localId, serverId),
        employeeId = employeeId,
        sourceTransactionId = sourceTransactionId,
        sourceTransactionType = sourceTransactionType,
        commissionAmount = commissionAmount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}

fun EmployeeAccountTransactionWithDetailsEntity.toDomain(): EmployeeAccountTransaction {
    return EmployeeAccountTransaction(
        id = Id(transactionEntity.localId, transactionEntity.serverId),
        employee = employee?.toDomain() ?: throw NullPointerException(),
        createdByEmployee = createdByEmployee?.toDomain() ?: throw NullPointerException(),
        type = transactionEntity.type,
        amount = transactionEntity.amount,
        relatedCommissionId = transactionEntity.relatedCommissionId,
        notes = transactionEntity.notes,
        createdAt = transactionEntity.createdAt,
        isSynced = transactionEntity.isSynced,
        updatedAt = transactionEntity.updatedAt
    )
}