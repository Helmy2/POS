package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain

@Entity(
    tableName = "receive_pay_vouchers", foreignKeys = [ForeignKey(
        entity = ClientEntity::class,
        parentColumns = ["localId"],
        childColumns = ["clientLocalId"],
        onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = SupplierEntity::class,
        parentColumns = ["localId"],
        childColumns = ["supplierLocalId"],
        onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["employeeLocalId"],
        onDelete = ForeignKey.RESTRICT
    )], indices = [Index("clientLocalId"), Index("supplierLocalId"), Index("employeeLocalId")]
)
data class ReceivePayVoucherEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Int?,
    val isReceipt: Boolean, // True for Receive (from client), False for Pay (to supplier)
    val clientLocalId: Long?,
    val supplierLocalId: Long?,
    val employeeLocalId: Long,
    val amount: Double,
    val date: Long,
    val notes: String?,
    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

data class ReceivePayVoucherWithDetails(
    @Embedded val voucher: ReceivePayVoucherEntity,

    @Relation(
        parentColumn = "clientLocalId",
        entityColumn = "localId",
        entity = ClientEntity::class
    )
    val client: ClientWithDetailsEntity?,

    @Relation(
        parentColumn = "supplierLocalId",
        entityColumn = "localId",
        entity = SupplierEntity::class
    )
    val supplier: SupplierWithDetailsEntity?,

    @Relation(
        parentColumn = "employeeLocalId",
        entityColumn = "id",
        entity = UserEntity::class
    )
    val createdByUser: UserEntity?
)

fun ReceivePayVoucherWithDetails.toDomain(): ReceivePayVoucher {
    val partyType: VoucherPartyType
    val party: Any

    if (this.voucher.isReceipt) {
        partyType = VoucherPartyType.CLIENT
        party = this.client?.toDomain()
            ?: throw IllegalStateException("Receipt voucher #${this.voucher.localId} must have a client.")
    } else {
        partyType = VoucherPartyType.SUPPLIER
        party = this.supplier?.toDomain()
            ?: throw IllegalStateException("Payment voucher #${this.voucher.localId} must have a supplier.")
    }

    val creator = this.createdByUser?.toDomain()
        ?: throw IllegalStateException("Voucher #${this.voucher.localId} must have a creator employee.")

    return ReceivePayVoucher(
        localId = this.voucher.localId,
        serverId = this.voucher.serverId,
        amount = this.voucher.amount,
        party = party,
        partyType = partyType,
        date = this.voucher.date,
        notes = this.voucher.notes,
        createdBy = creator,
        isSynced = this.voucher.isSynced
    )
}