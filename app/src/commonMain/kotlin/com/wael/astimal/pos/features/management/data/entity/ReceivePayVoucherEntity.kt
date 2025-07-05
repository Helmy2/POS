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
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain

@Entity(
    tableName = "receive_pay_vouchers", foreignKeys = [ForeignKey(
        entity = BusinessPartnerEntity::class,
        parentColumns = ["localId"],
        childColumns = ["partnerLocalId"],
    ), ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["employeeLocalId"],
    )], indices = [Index("partnerLocalId"), Index("employeeLocalId")]
)
data class ReceivePayVoucherEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,
    val partyType: VoucherPartyType,
    val partnerLocalId: Long,
    val employeeLocalId: Long,
    val amount: Double,
    val notes: String,
) : ItemEntity

data class ReceivePayVoucherWithDetails(
    @Embedded val voucher: ReceivePayVoucherEntity,

    @Relation(
        parentColumn = "partnerLocalId",
        entityColumn = "localId",
        entity = BusinessPartnerEntity::class
    ) val partner: BusinessPartnerWithDetailsEntity?,

    @Relation(
        parentColumn = "employeeLocalId", entityColumn = "id", entity = UserEntity::class
    ) val createdByUser: UserEntity?
)

fun ReceivePayVoucherWithDetails.toDomain(): ReceivePayVoucher {
    val creator = createdByUser?.toDomain()
        ?: throw IllegalStateException("Voucher #${voucher.localId} must have a creator employee.")

    return ReceivePayVoucher(
        id = Id(voucher.localId, voucher.serverId),
        amount = voucher.amount,
        party = partner?.toDomain() ?: throw IllegalStateException(
            "Voucher #${voucher.localId} must have a partner."
        ),
        partyType = voucher.partyType,
        notes = voucher.notes,
        createdBy = creator,
        createdAt = voucher.createdAt,
        updatedAt = voucher.updatedAt,
        isSynced = voucher.isSynced
    )
}