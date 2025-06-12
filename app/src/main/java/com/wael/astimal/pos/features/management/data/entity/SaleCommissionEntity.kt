package com.wael.astimal.pos.features.management.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.management.domain.entity.SaleCommission
import com.wael.astimal.pos.features.management.domain.entity.SourceTransactionType
import com.wael.astimal.pos.features.user.data.entity.UserEntity

@Entity(
    tableName = "employee_sale_commissions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index(value = ["employeeId"]), Index(value = ["sourceTransactionId", "sourceTransactionType"])]
)
data class SaleCommissionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Int?,
    val employeeId: Long,
    val sourceTransactionId: Long,
    val sourceTransactionType: SourceTransactionType,
    val commissionAmount: Double,
    val isMain: Boolean = false,
    val date: Long,
    var isSynced: Boolean = false
)

@Entity(
    tableName = "employee_account_transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByEmployeeId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = SaleCommissionEntity::class,
            parentColumns = ["localId"],
            childColumns = ["relatedCommissionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["employeeId"]), Index(value = ["createdByEmployeeId"])]
)
data class EmployeeAccountTransactionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Int?,
    val employeeId: Long,
    val createdByEmployeeId: Long,
    val type: EmployeeTransactionType,
    val amount: Double,
    @ColumnInfo(index = true) val relatedCommissionId: Long?,
    val notes: String?,
    val date: Long,
    var isSynced: Boolean = false
)

fun SaleCommissionEntity.toDomain(): SaleCommission {
    return SaleCommission(
        localId = this.localId,
        serverId = this.serverId,
        employeeId = this.employeeId,
        sourceTransactionId = this.sourceTransactionId,
        sourceTransactionType = this.sourceTransactionType,
        commissionAmount = this.commissionAmount,
        isMain = this.isMain,
        date = this.date,
        isSynced = this.isSynced
    )
}

fun EmployeeAccountTransactionEntity.toDomain(): EmployeeAccountTransaction {
    return EmployeeAccountTransaction(
        localId = this.localId,
        serverId = this.serverId,
        employeeId = this.employeeId,
        createdByEmployeeId = this.createdByEmployeeId,
        type = this.type,
        amount = this.amount,
        relatedCommissionId = this.relatedCommissionId,
        notes = this.notes,
        date = this.date,
        isSynced = this.isSynced
    )
}