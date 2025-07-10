package com.wael.astimal.pos.features.management.data.remote.dto

import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.management.data.local.entity.EmployeeTransactionEntity
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeTransactionDto(
    val id: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("employee_id") val employeeId: String,
    @SerialName("creator_id") val creatorId: String,
    @SerialName("invoice_id") val invoiceId: String?,
    @SerialName("transaction_type") val transactionType: String,
    val balance: Double,
    val notes: String?
)

fun EmployeeTransactionDto.toEntity(
    employeeId: Long,
    createdByEmployeeId: Long
): EmployeeTransactionEntity {
    return EmployeeTransactionEntity(
        serverId = id,
        employeeId = employeeId,
        createdByEmployeeId = createdByEmployeeId,
        type = EmployeeTransactionType.valueOf(transactionType.uppercase()),
        amount = balance,
        notes = notes,
        createdAt = createdAt.parseIsoTimestamp() ?: System.currentTimeMillis(),
        updatedAt = createdAt.parseIsoTimestamp() ?: System.currentTimeMillis(),
        isSynced = true,
        localId = 0L,
        invoiceId = invoiceId
    )
}