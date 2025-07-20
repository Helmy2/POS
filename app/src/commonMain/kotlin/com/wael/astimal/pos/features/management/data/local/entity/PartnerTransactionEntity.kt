package com.wael.astimal.pos.features.management.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.opening_balance
import pos.app.generated.resources.payment
import pos.app.generated.resources.purchase
import pos.app.generated.resources.purchase_return
import pos.app.generated.resources.sales
import pos.app.generated.resources.sales_return


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
    @PrimaryKey
    val localId: String,
    var isSynced: Boolean = false,
    val createdAt: Long = Clock.now(),
    val updatedAt: Long = Clock.now(),
    var isDeletedLocally: Boolean = false,
    val partnerLocalId: String,
    val employeeLocalId: String,
    val invoiceId: String?,
    val transactionType: TransactionType,
    val notes: String?,
    val balance: Double
)

enum class TransactionType {
    OPENING_BALANCE,
    PAYMENT,
    SALE_INVOICE,
    PURCHASE_INVOICE,
    SALE_RETURN_INVOICE,
    PURCHASE_RETURN_INVOICE;

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
            PAYMENT -> Res.string.payment
            SALE_INVOICE -> Res.string.sales
            PURCHASE_INVOICE -> Res.string.purchase
            SALE_RETURN_INVOICE -> Res.string.sales_return
            PURCHASE_RETURN_INVOICE -> Res.string.purchase_return
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
        id = voucher.localId,
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