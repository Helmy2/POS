package com.wael.astimal.pos.features.management.data.repository

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
                it.businessPartner.type != PartnerType.SUPPLIER
            }.map { it.toDomain() }
        }
    }

    override fun getSuppliers(query: String): Flow<List<BusinessPartner>> {
        return partnerDao.searchPartnersFlow(query).map { entities ->
            entities.filter {
                it.businessPartner.type != PartnerType.CLIENT
            }.map { it.toDomain() }
        }
    }

    override suspend fun saveBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return try {
            saveBusinessPartner(partner.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveBusinessPartner(
        partner: BusinessPartnerEntity
    ) {
        if (partner.localId == Id.new.local) {
            val clientId = partnerDao.insertOrUpdate(partner)

            createOpeningBalanceTransaction(
                partnerLocalId = clientId,
                debit = (partner.openingBalance.takeIf { it > 0 } ?: 0.0),
                credit = (partner.openingBalance.takeIf { it < 0 } ?: 0.0) * -1
            )
        } else {
            partnerTransactionDao.deleteTransactionsByPartner(
                partner.localId,
                TransactionType.OPENING_BALANCE
            )
            val clientId = partnerDao.insertOrUpdate(partner)

            createOpeningBalanceTransaction(
                partnerLocalId = clientId,
                debit = (partner.openingBalance.takeIf { it > 0 } ?: 0.0),
                credit = (partner.openingBalance.takeIf { it < 0 } ?: 0.0) * -1
            )
        }
    }

    override suspend fun syncWithServer(
        list: List<BusinessPartnerEntity>
    ): Result<Unit> {
        return runCatching {
            list.map { serverEntity ->
                val existingLocal = partnerDao.getPartnerBySeverId(
                    serverEntity.serverId ?: throw Exception("serverId is null")
                )

                serverEntity.copy(
                    localId = existingLocal?.localId ?: 0L,
                )
            }.forEach {
                saveBusinessPartner(it)
            }
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

    override suspend fun getPartnerBalance(partner: BusinessPartner): Result<Double> {
        return runCatching {
            partnerTransactionDao.getPartnerBalance(partner.id.local) ?: 0.0
        }
    }

    override suspend fun getBusinessPartnerByServerId(serverId: Long): Result<BusinessPartnerEntity> {
        return runCatching {
            partnerDao.getPartnerBySeverId(serverId) ?: throw Exception("Partner not found")
        }
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
        responsibleEmployeeLocalId = responsibleEmployee.id.local,
        createdAt = createdAt,
        updatedAt = updatedAt,
        type = type,
        openingBalance = openingBalance
    )
}