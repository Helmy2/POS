package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain

@Entity(
    tableName = "receive_pay_vouchers", foreignKeys = [ForeignKey(
        entity = ClientEntity::class,
        parentColumns = ["localId"],
        childColumns = ["clientLocalId"],
    ), ForeignKey(
        entity = SupplierEntity::class,
        parentColumns = ["localId"],
        childColumns = ["supplierLocalId"],
    ), ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["employeeLocalId"],
    )], indices = [Index("clientLocalId"), Index("supplierLocalId"), Index("employeeLocalId")]
)
data class ReceivePayVoucherEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,
    val partyType: VoucherPartyType,
    val clientLocalId: Long?,
    val supplierLocalId: Long?,
    val employeeLocalId: Long,
    val amount: Double,
    val notes: String,
) : ItemEntity

data class ReceivePayVoucherWithDetails(
    @Embedded val voucher: ReceivePayVoucherEntity,

    @Relation(
        parentColumn = "clientLocalId", entityColumn = "localId", entity = ClientEntity::class
    ) val client: ClientWithDetailsEntity?,

    @Relation(
        parentColumn = "supplierLocalId", entityColumn = "localId", entity = SupplierEntity::class
    ) val supplier: SupplierWithDetailsEntity?,

    @Relation(
        parentColumn = "employeeLocalId", entityColumn = "id", entity = UserEntity::class
    ) val createdByUser: UserEntity?
)

fun ReceivePayVoucherWithDetails.toDomain(): ReceivePayVoucher {
    val businessPartner = if (client != null && supplier != null) {
        val clint = client.toDomain()
        supplier.toDomain().copy(
            clientDebt = clint.clientDebt,
            clientLocalId = clint.clientLocalId
        )
    } else if (voucher.partyType == VoucherPartyType.CLIENT && client != null) {
        client.toDomain()
    } else if (voucher.partyType == VoucherPartyType.CLIENT && supplier != null) {
        supplier.toDomain()
    } else {
        throw IllegalStateException("Receipt voucher #${voucher.localId}")
    }

    val creator = createdByUser?.toDomain()
        ?: throw IllegalStateException("Voucher #${voucher.localId} must have a creator employee.")

    return ReceivePayVoucher(
        id = Id(voucher.localId, voucher.serverId),
        amount = voucher.amount,
        party = businessPartner,
        partyType = voucher.partyType,
        notes = voucher.notes,
        createdBy = creator,
        createdAt = voucher.createdAt,
        updatedAt = voucher.updatedAt,
        isSynced = voucher.isSynced
    )
}