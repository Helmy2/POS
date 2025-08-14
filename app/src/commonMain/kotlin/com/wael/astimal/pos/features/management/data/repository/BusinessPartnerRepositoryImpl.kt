package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.core.data.SyncManager
import com.wael.astimal.pos.core.util.deleteRecordAndLog
import com.wael.astimal.pos.features.management.data.local.dao.BusinessPartnerDao
import com.wael.astimal.pos.features.management.data.local.dao.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BusinessPartnerRepositoryImpl(
    private val userRepository: UserRepository,
    private val partnerDao: BusinessPartnerDao,
    private val partnerTransactionDao: PartnerTransactionDao,
    private val supabaseClient: SupabaseClient,
    private val syncManager: SyncManager
) : BusinessPartnerRepository {

    override suspend fun getBusinessPartners(query: String): Flow<List<BusinessPartner>> {
        val canHandlePrivate = userRepository.getCurrentUser()?.canHandlePrivatePartner ?: false

        return partnerDao.searchPartnersFlow(query).map { entities ->
            entities.map { it.toDomain() }.filter {
                canHandlePrivate || !it.isPrivate
            }
        }
    }

    override suspend fun getClients(query: String): Flow<List<BusinessPartner>> {
        return getBusinessPartners(query).map {
            it.filter {
                it.type != PartnerType.CLIENT || it.type == PartnerType.BOTH
            }
        }
    }

    override suspend fun getSuppliers(query: String): Flow<List<BusinessPartner>> {
        return getBusinessPartners(query).map {
            it.filter {
                it.type == PartnerType.SUPPLIER || it.type == PartnerType.BOTH
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveBusinessPartner(partner: BusinessPartner): Result<String> {
        return try {
            val entity = if (partner.id == "") {
                partner.toEntity().copy(localId = Uuid.random().toString())
            } else {
                partner.toEntity()
            }

            partnerDao.insertOrUpdate(entity)

            syncManager.requestSync()

            Result.success(entity.localId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(
        list: List<BusinessPartnerEntity>
    ): Result<Unit> {
        return runCatching {
            list.forEach { partnerDao.insertOrUpdate(it) }
        }.onFailure {
            it.printStackTrace()
        }
    }

    override suspend fun deleteBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return try {
            supabaseClient.deleteRecordAndLog(
                targetTableName = "business_partners",
                targetRecordId = partner.id
            )
            partnerDao.hardDelete(partner.id)
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

    override suspend fun getClient(clientId: String): BusinessPartner? {
        return partnerDao.getPartnerById(clientId)?.toDomain()
    }

    override suspend fun getPartnerBalance(partner: BusinessPartner): Result<Double> {
        return runCatching {
            partnerTransactionDao.getPartnerBalance(partner.id) ?: 0.0
        }
    }

    override suspend fun getBusinessPartnerByServerId(serverId: String): Result<BusinessPartnerEntity?> {
        return runCatching {
            partnerDao.getPartnerById(serverId)?.businessPartner
                ?: throw Exception("Partner not found")
        }
    }

    override suspend fun deleteAll(ids: List<String>): Result<Unit> {
        return try {
            ids.forEach { partnerDao.hardDelete(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}