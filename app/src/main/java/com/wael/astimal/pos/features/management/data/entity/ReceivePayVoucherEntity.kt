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
    tableName = "receive_pay_vouchers",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["localId"],
            childColumns = ["clientLocalId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["localId"],
            childColumns = ["supplierLocalId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByUserId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("clientLocalId"),
        Index("supplierLocalId"),
        Index("createdByUserId")
    ]
)
data class ReceivePayVoucherEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serverId: Int?,
    val amount: Double,
    val clientLocalId: Long?,
    val supplierLocalId: Long?,
    val partyType: VoucherPartyType,
    val date: Long,
    val notes: String?,
    val createdByUserId: Long,
    var isSynced: Boolean = false
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
        parentColumn = "createdByUserId",
        entityColumn = "id",
        entity = UserEntity::class
    )
    val createdByUser: UserEntity
)

fun ReceivePayVoucherWithDetails.toDomain(): ReceivePayVoucher {
    val party = when(voucher.partyType) {
        VoucherPartyType.CLIENT -> client?.toDomain()
            ?: throw IllegalStateException("Client cannot be null for a client voucher")
        VoucherPartyType.SUPPLIER -> supplier?.toDomain()
            ?: throw IllegalStateException("Supplier cannot be null for a supplier voucher")
    }
    return ReceivePayVoucher(
        localId = voucher.localId,
        serverId = voucher.serverId,
        amount = voucher.amount,
        party = party,
        partyType = voucher.partyType,
        date = voucher.date,
        notes = voucher.notes,
        createdBy = createdByUser.toDomain(),
        isSynced = voucher.isSynced
    )
}