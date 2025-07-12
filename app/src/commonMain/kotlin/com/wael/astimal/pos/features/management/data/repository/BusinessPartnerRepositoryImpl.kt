package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.management.data.local.dao.BusinessPartnerDao
import com.wael.astimal.pos.features.management.data.local.dao.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return try {
            val entity = if (partner.id == Id.new) {
                partner.toEntity().copy(
                    serverId = Uuid.random().toString()
                )
            } else {
                partner.toEntity()
            }

            partnerDao.insertOrUpdate(entity)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
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
                partnerDao.insertOrUpdate(it)
            }
        }
    }

    override suspend fun deleteBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return runCatching {
            partnerDao.softDeletePartnerByLocalId(partner.id.local)
        }.onFailure {
            it.printStackTrace()
        }
    }


    override suspend fun hardDeleteByServerId(serverId: String): Result<Unit> {
        return try {
            partnerDao.hardDeletePartnerByServerId(serverId)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAllUnSynced(): Result<List<BusinessPartner>> {
        return try {
            Result.success(partnerDao.getAllUnSynced().map { it.toDomain() })
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAllDeletedPartners(): Result<List<BusinessPartner>> {
        return try {
            Result.success(partnerDao.getAllDeletedPartners().map { it.toDomain() })
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getClient(clientId: Long): BusinessPartner? {
        return partnerDao.getPartnerByLocalId(clientId)?.toDomain()
    }

    override suspend fun getPartnerBalance(partner: BusinessPartner): Result<Double> {
        return runCatching {
            partnerTransactionDao.getPartnerBalance(partner.id.local) ?: 0.0
        }
    }

    override suspend fun getBusinessPartnerByServerId(serverId: String): Result<BusinessPartnerEntity?> {
        return runCatching {
            partnerDao.getPartnerBySeverId(serverId) ?: throw Exception("Partner not found")
        }
    }
}