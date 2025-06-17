package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
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
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override var isDeletedLocally: Boolean = false,
    val isReceipt: Boolean, // True for Receive (from client), False for Pay (to supplier)
    val clientLocalId: Long?,
    val supplierLocalId: Long?,
    val employeeLocalId: Long,
    val amount: Double,
    val notes: String,
) : ItemEntity

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

    if (voucher.isReceipt) {
        partyType = VoucherPartyType.CLIENT
        party = client?.toDomain()
            ?: throw IllegalStateException("Receipt voucher #${voucher.localId} must have a client.")
    } else {
        partyType = VoucherPartyType.SUPPLIER
        party = supplier?.toDomain()
            ?: throw IllegalStateException("Payment voucher #${voucher.localId} must have a supplier.")
    }

    val creator = createdByUser?.toDomain()
        ?: throw IllegalStateException("Voucher #${voucher.localId} must have a creator employee.")

    return ReceivePayVoucher(
        id = Id(voucher.localId, voucher.serverId),
        amount = voucher.amount,
        party = party,
        partyType = partyType,
        notes = voucher.notes,
        createdBy = creator,
        createdAt = voucher.createdAt,
        updatedAt = voucher.updatedAt,
        isSynced = voucher.isSynced
    )
}