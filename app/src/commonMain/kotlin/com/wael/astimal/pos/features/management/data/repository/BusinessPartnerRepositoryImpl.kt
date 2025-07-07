package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.management.data.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.BusinessPartnerDao
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.remote.dto.BusinessPartnerDto
import com.wael.astimal.pos.features.management.data.remote.dto.toEntity
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.entity.toDto
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BusinessPartnerRepositoryImpl(
    private val partnerDao: BusinessPartnerDao,
    private val supabaseClient: SupabaseClient,
    private val userRepository: UserRepository,
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
            val entity = partner.toDto()

            val result = if (partner.id == Id.new) {
                supabaseClient.from("business_partners").insert(entity) {
                    select()
                }.decodeSingle<BusinessPartnerDto>()
            } else {
                println(entity)
                supabaseClient.from("business_partners").update(entity) {
                    filter {
                        eq("id", entity.id)
                    }
                    select()
                }.decodeSingle<BusinessPartnerDto>()
            }

            partnerDao.insertOrUpdate(
                result.toEntity(
                    responsibleId = userRepository.getUserByServerId(
                        result.responsibleId
                    ).getOrThrow()!!.id.local
                ).copy(localId = partner.id.local)
            )

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
            TODO()
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

    override suspend fun getBusinessPartnerByServerId(serverId: Long): Result<BusinessPartnerEntity> {
        return runCatching {
            partnerDao.getPartnerBySeverId(serverId) ?: throw Exception("Partner not found")
        }
    }
}