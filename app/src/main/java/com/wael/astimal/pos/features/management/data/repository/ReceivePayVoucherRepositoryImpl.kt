package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.ReceivePayVoucherDao
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.ReceivePayVoucherRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReceivePayVoucherRepositoryImpl(
    private val database: AppDatabase,
    private val voucherDao: ReceivePayVoucherDao,
    private val clientRepository: ClientRepository,
    private val supplierRepository: SupplierRepository
) : ReceivePayVoucherRepository {

    override fun getVouchers(): Flow<List<ReceivePayVoucher>> {
        return voucherDao.getAllVouchersWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addVoucher(voucher: ReceivePayVoucher): Result<Unit> {
        return try {
            database.withTransaction {
                val voucherEntity = ReceivePayVoucherEntity(
                    serverId = null,
                    amount = voucher.amount,
                    clientLocalId = if (voucher.party is Client) voucher.party.id else null,
                    supplierLocalId = if (voucher.party is Supplier) voucher.party.id else null,
                    partyType = voucher.partyType,
                    date = voucher.date,
                    notes = voucher.notes,
                    createdByUserId = voucher.createdBy.id
                )
                voucherDao.insertVoucher(voucherEntity)

                when (voucher.partyType) {
                    VoucherPartyType.CLIENT -> {
                        val client = voucher.party as Client
                        // We subtract because a payment from a client reduces their debt
                        clientRepository.adjustClientDebt(client.id, -voucher.amount)
                    }
                    VoucherPartyType.SUPPLIER -> {
                        val supplier = voucher.party as Supplier
                        // We subtract because a payment to a supplier reduces our indebtedness
                        supplierRepository.adjustSupplierIndebtedness(supplier.id, -voucher.amount)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
