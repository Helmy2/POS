package com.wael.astimal.pos.features.management.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.invoice
import pos.app.generated.resources.opening_balance
import pos.app.generated.resources.payment


@Entity(
    tableName = "partner_transactions", foreignKeys = [ForeignKey(
        entity = BusinessPartnerEntity::class,
        parentColumns = ["localId"],
        childColumns = ["partnerLocalId"],
    ), ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["employeeLocalId"],
    )], indices = [Index("partnerLocalId"), Index("employeeLocalId")]
)
data class PartnerTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: String?,
    var isSynced: Boolean = false,
    val createdAt: Long = Clock.now(),
    val updatedAt: Long = Clock.now(),
    var isDeletedLocally: Boolean = false,
    val partnerLocalId: Long,
    val employeeLocalId: Long,
    val invoiceId: String?,
    val transactionType: TransactionType,
    val notes: String?,
    val balance: Double
)

enum class TransactionType {
    OPENING_BALANCE,
    INVOICE,
    PAYMENT;

    companion object {
        fun getTypesForDropdown(): List<TransactionType> {
            return listOf(
                OPENING_BALANCE,
                PAYMENT
            )
        }
    }

    fun getStringRes(): StringResource {
        return when (this) {
            OPENING_BALANCE -> Res.string.opening_balance
            INVOICE -> Res.string.invoice
            PAYMENT -> Res.string.payment
        }
    }
}

data class PartnerTransactionWithDetails(
    @Embedded val voucher: PartnerTransactionEntity,

    @Relation(
        parentColumn = "partnerLocalId",
        entityColumn = "localId",
        entity = BusinessPartnerEntity::class
    ) val partner: BusinessPartnerWithDetailsEntity?,

    @Relation(
        parentColumn = "employeeLocalId", entityColumn = "id", entity = UserEntity::class
    ) val createdByUser: UserEntity?
)

fun PartnerTransactionWithDetails.toDomain(): ReceivePayVoucher {
    val creator = createdByUser?.toDomain()
        ?: throw IllegalStateException("Voucher #${voucher.localId} must have a creator employee.")

    return ReceivePayVoucher(
        id = Id(voucher.localId, serverStringId = voucher.serverId),
        amount = voucher.balance,
        partner = partner?.toDomain() ?: throw IllegalStateException(
            "Voucher #${voucher.localId} must have a partner."
        ),
        notes = voucher.notes ?: "",
        createdBy = creator,
        createdAt = voucher.createdAt,
        updatedAt = voucher.updatedAt,
        isSynced = voucher.isSynced,
        transactionType = voucher.transactionType,
        invoiceId = voucher.invoiceId ?: ""
    )
}