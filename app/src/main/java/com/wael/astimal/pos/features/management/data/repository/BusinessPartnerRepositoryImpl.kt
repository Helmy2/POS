package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.data.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.BusinessPartnerDao
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BusinessPartnerRepositoryImpl(
    private val db: AppDatabase,
    private val partnerDao: BusinessPartnerDao,
    private val partnerTransactionDao: PartnerTransactionDao,
) : BusinessPartnerRepository {

    override fun getBusinessPartners(query: String): Flow<List<BusinessPartner>> {
        return partnerDao.searchPartnersFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getClients(query: String): Flow<List<BusinessPartner>> {
        return partnerDao.searchPartnersFlow(query).map { entities ->
            entities.filter {
                it.businessPartner.type != PartnerType.SUPPLIER && it.businessPartner.type != PartnerType.SUPPLIER_AND_CAN_BE_CLIENT
            }.map { it.toDomain() }

        }
    }

    override fun getSuppliers(query: String): Flow<List<BusinessPartner>> {
        return partnerDao.searchPartnersFlow(query).map { entities ->
            entities.filter {
                it.businessPartner.type != PartnerType.CLIENT && it.businessPartner.type != PartnerType.CLIENT_AND_CAN_BE_SUPPLIER
            }.map { it.toDomain() }
        }
    }

    override suspend fun saveBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return try {
            db.withTransaction {
                if (partner.id == Id.new) {
                    val clientId = partnerDao.insertOrUpdate(partner.toEntity())

                    createOpeningBalanceTransaction(
                        partnerLocalId = clientId,
                        debit = (partner.openingBalance.takeIf { it < 0 } ?: 0.0) * -1,
                        credit = partner.openingBalance.takeIf { it > 0 } ?: 0.0
                    )
                } else {
                    TODO()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return runCatching {
            TODO()
        }
    }

    override suspend fun getClient(clientId: Long): BusinessPartner? {
        return partnerDao.getPartnerByLocalId(clientId)?.toDomain()
    }


    private suspend fun createOpeningBalanceTransaction(
        partnerLocalId: Long? = null,
        debit: Double,
        credit: Double,
    ) {
        if (debit == 0.0 && credit == 0.0) return

        partnerTransactionDao.insertTransaction(
            PartnerTransactionEntity(
                serverId = null,
                partnerLocalId = partnerLocalId,
                sourceTransactionId = 0L,
                transactionType = TransactionType.OPENING_BALANCE,
                createdAt = Clock.now(),
                updatedAt = Clock.now(),
                debit = debit,
                credit = credit,
            )
        )
    }
}

private fun BusinessPartner.toEntity(): BusinessPartnerEntity {
    return BusinessPartnerEntity(
        localId = id.local,
        serverId = id.server,
        arName = name.arName.orEmpty(),
        enName = name.enName.orEmpty(),
        phone = phone,
        address = address,
        responsibleEmployeeLocalId = responsibleEmployee.id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        type = type,
        openingBalance = openingBalance
    )
}